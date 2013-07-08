library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

-- 3-15-04 Shifted to shift register implementation
-- 3-11-04 Replaced FIFOs with simple arrays
-- 3-9-04 Added I2S Signals and Playback and Record Control, REB
-- 3-7-04 Added intermodule sync of WE and STB inputs, REB
--  Uncomment the following lines to use the declarations that are
--  provided for instantiating Xilinx primitive components.
--library UNISIM;
--use UNISIM.VComponents.all;

entity RBX_APCIPB is port
(
	-- System Control Inputs
	RST_I : in std_logic; -- Master reset for the peripheral bus
	CLK_I : in std_logic; -- Master clock for the peripheral bus

	-- Perpiheral Bus Signals					  
	WE_I : in std_logic; -- Write/Read indication for strobe (1=Write)
	CYC_I : in std_logic; -- Cycle indicator
	ADR_I : in std_logic_vector(17 downto 0); -- Peripheral block address
	SEL_I : in std_logic_vector(3 downto 0); -- Peripheral byte selects (active high)
	DAT_I : in std_logic_vector(31 downto 0); -- Peripheral block data input
	STB_I : in std_logic; -- Read/Write strobe (Active High)
	DAT_O : out std_logic_vector(31 downto 0); -- Peripheral block data output
	ACK_O : out std_logic; -- Peripheral block ACK (Active High)

	-- Peripheral Block Signals
	PB_EN_I : in std_logic; -- Perpheral block enable (Active High)
	PB_INT_O : out std_logic; -- Peripheral block interrupt output (active high)

	-- External Signals
	DA_INT_8415A : in std_logic; -- CS8415A interrupt signal (active high)
	DA_INT_8405A : in std_logic; -- CS8405A interrupt signal (active high)
	DAI_ENABLE : out std_logic; -- Enable the CS8415A
	
	-- I2S Signals to/from CPU
	XS_BITCLK		:inout std_logic; -- GPIO28/AC97 Audio Port or I2S bit clock (input or output) 
	XS_SDATA_IN0	:out	std_logic;	-- GPIO29/AC97 or I2S data Input  (input to cpu)
	XS_SDATA_IN1	:in	std_logic; 	-- GPIO32/AC97 Audio Port data in. I2S sysclk output from cpu
	XS_SDATA_OUT	:in	std_logic; 	-- GPIO30/AC97 Audio Port or I2S data out from cpu
	XS_SYNC		:in	std_logic;	-- GPIO31/AC97 Audio Port or I2S Frame sync signal. (output from cpu)

	-- I2S Signals to/from APP BD
	I2S_MCLK_GCK3	:in  std_logic; 	--LOC="C8" GCK3 
	I2S_SCLK_GCK1	:in  std_logic;	--LOC="T8" GCK1 
	I2S_MCLK 		:out std_logic;
	I2S_SCLK 		:inout std_logic;
	I2S_LRCLK 		:inout std_logic;
	I2S_SDATA 		:inout std_logic	
);
end RBX_APCIPB;

architecture Rtl of RBX_APCIPB is

	-- Register Address: 
	constant CNTL_REG_ADDR : bit_vector (17 downto 0) := "000000000000000000"; -- 0x0
	constant STAT_REG_ADDR : bit_vector (17 downto 0) := "000000000000000100"; -- 0x4

	-- Peripheral Bus Slave State Machine Types
	type SLAVE_StateType is 
	(
		SLAVE_IDLE, 
		WRITE_START,
		WRITE_STROBE,
		WRITE_ACK,
		READ_START,
		READ_STROBE,
		READ_ACK
	);

	-- Peripheral Signals
	signal SLAVE_currentState : SLAVE_StateType;
	signal SLAVE_nextState : SLAVE_StateType;
	signal CNTL_REG : std_logic_vector(31 downto 0); -- Control Register
	signal STAT_REG : std_logic_vector(31 downto 0); -- Status Register
	signal ADR_I_BV : bit_vector(17 downto 0); -- Bit vector version of adr_i for case
	signal MASKED_INT : std_logic_vector(1 downto 0) ; -- Masked interrupt

	signal WE_I_IN,STB_I_IN:std_logic; --3-5-04 Added intermodule sync , REB

-- Intermediate I2S Output Signals
	signal XS_SDATA_IN0_out,
		XS_BITCLK_out,
		I2S_MCLK_out,
		I2S_SCLK_out,
		I2S_LRCLK_out,
		I2S_SDATA_out: std_logic;

signal DAI_ENABLE_out:std_logic;
signal I2S_SCLK_D1,I2S_LRCLK_D1: std_logic;
signal I2S_CTR,XS_CTR: std_logic_vector(4 downto 0);
signal LDATA,RDATA: std_logic_vector(31 downto 0);
signal XS_SYNC_D1:std_logic;
signal LDATA_out,RDATA_out: std_logic;

signal XS_BITCLK_out_D1:std_logic;

signal SCLK_BITCLK_D1,SCLK_BITCLK_D2,SCLK_BITCLK_D3:std_logic;
signal RWE,LWE: std_logic_vector(31 downto 0);
signal ROE,LOE: std_logic_vector(31 downto 0);


begin

-------------------------------------------------
--3-7-04 REB, Added intermodule sync for WE_I,STB_I, 
-------------------------------------------------
WE_I_IN_REG: process (CLK_I,WE_I) 
begin 
if (CLK_I 'event and CLK_I = '1') then WE_I_IN <= WE_I;
end if;
end process WE_I_IN_REG;
-------------------------------------------------
STB_I_IN_REG: process (CLK_I,STB_I) 
begin 
if (CLK_I 'event and CLK_I = '1') then STB_I_IN <= STB_I;
end if;
end process STB_I_IN_REG;
-------------------------------------------------

	-- Convert address to bit vector
	ADR_I_BV <= to_bitvector(ADR_I);

	-- Slave State Machine Synchronization 
	SLAVE_currentStateProc: process(RST_I, CLK_I, SLAVE_nextState)
	begin
		if (RST_I = '1') then 
			SLAVE_currentState <= SLAVE_IDLE;
		elsif (CLK_I'event and CLK_I = '1') then 
			SLAVE_currentState <= SLAVE_nextState;
		end if;
	end process SLAVE_currentStateProc;

	-- Slave State Maching Transitions
	SLAVE_nextStateProc: process(SLAVE_currentState,STB_I_IN,WE_I_IN) 
	begin
		case SLAVE_currentState is
			when SLAVE_IDLE =>
				if STB_I_IN = '1' and WE_I_IN = '1' then  
					SLAVE_nextState <= WRITE_START;
				elsif STB_I_IN = '1' and WE_I_IN = '0' then
					SLAVE_nextState <= READ_START;
				end if;

			when WRITE_START => 
				SLAVE_nextState <= WRITE_STROBE;

			when WRITE_STROBE => 
				SLAVE_nextState <= WRITE_ACK;

			when WRITE_ACK =>
				if STB_I_IN = '0' then
					SLAVE_nextState <=  SLAVE_IDLE;
				end if;

			when READ_START => 
				SLAVE_nextState <= READ_STROBE;

			when READ_STROBE => 
				SLAVE_nextState <= READ_ACK;

			when READ_ACK => 
				if STB_I_IN = '0' then 
					SLAVE_nextState <=  SLAVE_IDLE;
				end if;

			when others => 
				SLAVE_nextState <=  SLAVE_IDLE;
		end case;
	end process SLAVE_nextStateProc;

	-- Generate ACK output
	ACK_O_GEN: process (SLAVE_currentState)
	begin
		case SLAVE_currentState is
			when WRITE_ACK => 
				ACK_O <= '1';
			when READ_ACK => 
				ACK_O <= '1';
			when others =>
				ACK_O <= '0';
		end case;
	end process ACK_O_GEN;

	-- Generate data output
	DAT_O_GEN: process (ADR_I_BV, CNTL_REG, STAT_REG)
	begin
		case ADR_I_BV is
			when CNTL_REG_ADDR => 
				DAT_O <= CNTL_REG;
			when STAT_REG_ADDR =>
				DAT_O <= STAT_REG;
			when others => 
				DAT_O <= (others => '0');
		end case;
	end process DAT_O_GEN;

	-- Generate Control Register Write
	CNTL_REG_GEN: process (RST_I, CLK_I, PB_EN_I, SEL_I, SLAVE_currentState, ADR_I_BV, DAT_I)
	begin
		if (RST_I = '1' or PB_EN_I = '0') then
			CNTL_REG <= (others => '0');
		elsif (CLK_I'event and CLK_I = '1') then
			if (SLAVE_currentState = WRITE_STROBE) and (ADR_I_BV = CNTL_REG_ADDR) then
				if SEL_I(3) = '1' then
					CNTL_REG(31 downto 24) <= DAT_I(31 downto 24);
				end if;
				if SEL_I(2) = '1' then
					CNTL_REG(23 downto 16) <= DAT_I(23 downto 16);
				end if;
				if SEL_I(1) = '1' then
					CNTL_REG(15 downto 8) <= DAT_I(15 downto 8);
				end if;
				if SEL_I(0) = '1' then
					CNTL_REG(7 downto 0) <= DAT_I(7 downto 0);
				end if;
			end if;
		end if;
	end process CNTL_REG_GEN;

	-- Generate Status Register (Read Only)
	STAT_REG_GEN: process (RST_I, CLK_I, PB_EN_I, DA_INT_8415A, DA_INT_8405A)
	begin
		if (RST_I = '1' or PB_EN_I = '0') then
			STAT_REG <= (others => '0');
		elsif (CLK_I'event and CLK_I = '1') then
			if (DA_INT_8415A = '1') then
				STAT_REG(2) <= '1';
			else
				STAT_REG(2) <= '0';
			end if;
			if (DA_INT_8405A = '1') then
				STAT_REG(3) <= '1';
			else
				STAT_REG(3) <= '0';
			end if;
		end if;
	end process STAT_REG_GEN;

	-- Generate interrupt output
	MASKED_INT <= CNTL_REG(3 downto 2) and STAT_REG(3 downto 2);
	INT_OUTPUT_GEN: process (RST_I, CLK_I, PB_EN_I, MASKED_INT)
	begin
		if (RST_I = '1' or PB_EN_I = '0') then
			PB_INT_O <= '0';
		elsif (CLK_I'event and CLK_I = '1') then
			if (MASKED_INT = "00") then
				PB_INT_O <= '0';
			else
				PB_INT_O <= '1';
			end if;
		end if;
	end process INT_OUTPUT_GEN;

	-- Generate digital audio input enable
	DAI_ENABLE_OUTPUT_GEN: process (RST_I, CLK_I, PB_EN_I, CNTL_REG)
	begin
		if (RST_I = '1' or PB_EN_I = '0') then
			DAI_ENABLE_out <= '0';
		elsif (CLK_I'event and CLK_I = '1') then
			if (CNTL_REG(0) = '1') then
				DAI_ENABLE_out <= '0';
			else
				DAI_ENABLE_out <= '1';
			end if;
		end if;
	end process DAI_ENABLE_OUTPUT_GEN;		

DAI_ENABLE <= DAI_ENABLE_out;

---------------------------------------------------------
--I2S LOGIC:
---------------------------------------------------------

-- TRISTATE OUTPUT BUFFERS:

BITCLK_out_TRISTATE_BUF:process(DAI_ENABLE_out,XS_BITCLK_out)
begin
if DAI_ENABLE_out = '0' then XS_BITCLK <= XS_BITCLK_out;
else XS_BITCLK <= 'Z';
end if;
end process BITCLK_out_TRISTATE_BUF;

XS_SDATA_IN0_out_TRISTATE_BUF:process(DAI_ENABLE_out,XS_SDATA_IN0_out)
begin
if DAI_ENABLE_out = '0' then XS_SDATA_IN0 <= XS_SDATA_IN0_out;
else XS_SDATA_IN0 <= 'Z';
end if;
end process XS_SDATA_IN0_out_TRISTATE_BUF;

I2S_MCLK_out_TRISTATE_BUF:process(DAI_ENABLE_out,I2S_MCLK_out)
begin
if DAI_ENABLE_out = '1' then I2S_MCLK <= I2S_MCLK_out;
else I2S_MCLK <= 'Z';
end if;
end process I2S_MCLK_out_TRISTATE_BUF;

I2S_SCLK_out_TRISTATE_BUF:process(DAI_ENABLE_out,I2S_SCLK_out)
begin
if DAI_ENABLE_out = '1' then I2S_SCLK <= I2S_SCLK_out;
else I2S_SCLK <= 'Z';
end if;
end process I2S_SCLK_out_TRISTATE_BUF;

I2S_LRCLK_out_TRISTATE_BUF:process(DAI_ENABLE_out,I2S_LRCLK_out)
begin
if DAI_ENABLE_out = '1' then I2S_LRCLK <= I2S_LRCLK_out;
else I2S_LRCLK <= 'Z';
end if;
end process I2S_LRCLK_out_TRISTATE_BUF;

I2S_SDATA_out_TRISTATE_BUF:process(DAI_ENABLE_out,I2S_SDATA_out)
begin
if DAI_ENABLE_out = '1' then I2S_SDATA <= I2S_SDATA_out;
else I2S_SDATA <= 'Z';
end if;
end process I2S_SDATA_out_TRISTATE_BUF;

-- SIMPLE PLAYBACK PASS THROUGH (NOT SYNC'D)

I2S_MCLK_out_GEN:process(XS_SDATA_IN1)
begin
I2S_MCLK_out <= XS_SDATA_IN1;
end process I2S_MCLK_out_GEN;

I2S_SCLK_out_GEN:process(XS_BITCLK)
begin
I2S_SCLK_out <= XS_BITCLK;
end process I2S_SCLK_out_GEN;

I2S_LRCLK_out_GEN:process(XS_SYNC)
begin
I2S_LRCLK_out <= XS_SYNC;
end process I2S_LRCLK_out_GEN;

I2S_SDATA_out_GEN:process(XS_SDATA_OUT)
begin
I2S_SDATA_out <= XS_SDATA_OUT;
end process I2S_SDATA_out_GEN;


LDATA_GEN: for I in 31 downto 0 generate 
process(I2S_MCLK_GCK3,I2S_SCLK_D1 ,I2S_SCLK,LWE,I2S_SDATA)
begin
if I2S_MCLK_GCK3'event and I2S_MCLK_GCK3 = '1' then
	if (I2S_SCLK_D1 = '0' and I2S_SCLK = '1') and LWE(I) = '1' then LDATA(I) <= I2S_SDATA;
 	end if;
end if;
end process;
end generate LDATA_GEN;

RDATA_GEN: for I in 31 downto 0 generate 
process(I2S_MCLK_GCK3,I2S_SCLK_D1 ,I2S_SCLK,RWE,I2S_SDATA)
begin
if I2S_MCLK_GCK3'event and I2S_MCLK_GCK3 = '1' then
	if (I2S_SCLK_D1 = '0' and I2S_SCLK = '1') and RWE(I) = '1' then RDATA(I) <= I2S_SDATA;
 	end if;
end if;
end process;
end generate RDATA_GEN;


LWE_0_GEN: process (I2S_MCLK_GCK3,I2S_LRCLK,I2S_LRCLK_D1)
begin
if I2S_MCLK_GCK3'event and I2S_MCLK_GCK3 = '1' then
LWE(0) <= not(I2S_LRCLK) and I2S_LRCLK_D1;
end if;
end process LWE_0_GEN;

RWE_0_GEN: process (I2S_MCLK_GCK3,I2S_LRCLK,I2S_LRCLK_D1)
begin
if I2S_MCLK_GCK3'event and I2S_MCLK_GCK3 = '1' then
RWE(0) <= (I2S_LRCLK) and not(I2S_LRCLK_D1);
end if;
end process RWE_0_GEN;

LWE_I_GEN: for I in 31 downto 1 generate
process (I2S_MCLK_GCK3,I2S_SCLK_D1 ,I2S_SCLK,LWE)
begin
if I2S_MCLK_GCK3'event and I2S_MCLK_GCK3 = '1' then
	if (I2S_SCLK_D1 = '0' and I2S_SCLK = '1') then
 			LWE(I) <= LWE(I-1);
	end if;
end if; 
end process;
end generate LWE_I_GEN;

RWE_I_GEN: for I in 31 downto 1 generate
process (I2S_MCLK_GCK3,I2S_SCLK_D1 ,I2S_SCLK,RWE)
begin
if I2S_MCLK_GCK3'event and I2S_MCLK_GCK3 = '1' then

	if (I2S_SCLK_D1  = '0' and I2S_SCLK = '1') then
 			RWE(I) <= RWE(I-1);
	end if;
end if;
end process;
end generate RWE_I_GEN;


I2S_LRCLK_D1_GEN:process (I2S_MCLK_GCK3, I2S_SCLK,I2S_SCLK_D1 ,I2S_LRCLK)
begin
if I2S_MCLK_GCK3'event and I2S_MCLK_GCK3 = '1' then
	if (I2S_SCLK_D1 = '0' and I2S_SCLK = '1') then
 		I2S_LRCLK_D1 <= I2S_LRCLK;
	end if; 
end if;
end process I2S_LRCLK_D1_GEN;

I2S_SCLK_D1_GEN: process (I2S_SCLK,I2S_MCLK_GCK3)
begin
if I2S_MCLK_GCK3'event and I2S_MCLK_GCK3 = '1' then
	I2S_SCLK_D1 <= I2S_SCLK;
end if;
end process I2S_SCLK_D1_GEN;


-- SDATA INTO CPU
XS_SDATA_IN0_out_GEN:process(I2S_MCLK_GCK3,XS_SYNC,RDATA,LDATA,ROE,LOE)
begin
if I2S_MCLK_GCK3'event and I2S_MCLK_GCK3 = '1' then

	if XS_SYNC = '0' then -- XS_SYNC = '0' =>left data
		case LOE is 
		when "00000000000000000000000000000001"	=> XS_SDATA_IN0_out <= LDATA(0);
		when "00000000000000000000000000000010" 	=> XS_SDATA_IN0_out <= LDATA(1);
		when "00000000000000000000000000000100" 	=> XS_SDATA_IN0_out <= LDATA(2);
		when "00000000000000000000000000001000" 	=> XS_SDATA_IN0_out <= LDATA(3);
		when "00000000000000000000000000010000" 	=> XS_SDATA_IN0_out <= LDATA(4);
		when "00000000000000000000000000100000" 	=> XS_SDATA_IN0_out <= LDATA(5);
		when "00000000000000000000000001000000" 	=> XS_SDATA_IN0_out <= LDATA(6);
		when "00000000000000000000000010000000" 	=> XS_SDATA_IN0_out <= LDATA(7);
		when "00000000000000000000000100000000" 	=> XS_SDATA_IN0_out <= LDATA(8);
		when "00000000000000000000001000000000" 	=> XS_SDATA_IN0_out <= LDATA(9);
		when "00000000000000000000010000000000" 	=> XS_SDATA_IN0_out <= LDATA(10);
		when "00000000000000000000100000000000" 	=> XS_SDATA_IN0_out <= LDATA(11);
		when "00000000000000000001000000000000" 	=> XS_SDATA_IN0_out <= LDATA(12);
		when "00000000000000000010000000000000" 	=> XS_SDATA_IN0_out <= LDATA(13);
		when "00000000000000000100000000000000" 	=> XS_SDATA_IN0_out <= LDATA(14);
		when "00000000000000001000000000000000" 	=> XS_SDATA_IN0_out <= LDATA(15);
		when "00000000000000010000000000000000" 	=> XS_SDATA_IN0_out <= LDATA(16);
		when "00000000000000100000000000000000" 	=> XS_SDATA_IN0_out <= LDATA(17);
		when "00000000000001000000000000000000" 	=> XS_SDATA_IN0_out <= LDATA(18);
		when "00000000000010000000000000000000" 	=> XS_SDATA_IN0_out <= LDATA(19);
		when "00000000000100000000000000000000" 	=> XS_SDATA_IN0_out <= LDATA(20);
		when "00000000001000000000000000000000" 	=> XS_SDATA_IN0_out <= LDATA(21);
		when "00000000010000000000000000000000" 	=> XS_SDATA_IN0_out <= LDATA(22);
		when "00000000100000000000000000000000" 	=> XS_SDATA_IN0_out <= LDATA(23);
		when "00000001000000000000000000000000" 	=> XS_SDATA_IN0_out <= LDATA(24);
		when "00000010000000000000000000000000" 	=> XS_SDATA_IN0_out <= LDATA(25);
		when "00000100000000000000000000000000" 	=> XS_SDATA_IN0_out <= LDATA(26);
		when "00001000000000000000000000000000" 	=> XS_SDATA_IN0_out <= LDATA(27);
		when "00010000000000000000000000000000" 	=> XS_SDATA_IN0_out <= LDATA(28);
		when "00100000000000000000000000000000" 	=> XS_SDATA_IN0_out <= LDATA(29);
		when "01000000000000000000000000000000" 	=> XS_SDATA_IN0_out <= LDATA(30);
		when "10000000000000000000000000000000" 	=> XS_SDATA_IN0_out <= LDATA(31);
		when others 					=> XS_SDATA_IN0_out <= LDATA(0);
		end case;
	else			 -- XS_SYNC = '1' => right data
		case ROE is 
		when "00000000000000000000000000000001" 	=> XS_SDATA_IN0_out <= RDATA(0);
		when "00000000000000000000000000000010" 	=> XS_SDATA_IN0_out <= RDATA(1);
		when "00000000000000000000000000000100" 	=> XS_SDATA_IN0_out <= RDATA(2);
		when "00000000000000000000000000001000" 	=> XS_SDATA_IN0_out <= RDATA(3);
		when "00000000000000000000000000010000" 	=> XS_SDATA_IN0_out <= RDATA(4);
		when "00000000000000000000000000100000" 	=> XS_SDATA_IN0_out <= RDATA(5);
		when "00000000000000000000000001000000" 	=> XS_SDATA_IN0_out <= RDATA(6);
		when "00000000000000000000000010000000" 	=> XS_SDATA_IN0_out <= RDATA(7);
		when "00000000000000000000000100000000" 	=> XS_SDATA_IN0_out <= RDATA(8);
		when "00000000000000000000001000000000" 	=> XS_SDATA_IN0_out <= RDATA(9);
		when "00000000000000000000010000000000" 	=> XS_SDATA_IN0_out <= RDATA(10);
		when "00000000000000000000100000000000" 	=> XS_SDATA_IN0_out <= RDATA(11);
		when "00000000000000000001000000000000" 	=> XS_SDATA_IN0_out <= RDATA(12);
		when "00000000000000000010000000000000" 	=> XS_SDATA_IN0_out <= RDATA(13);
		when "00000000000000000100000000000000" 	=> XS_SDATA_IN0_out <= RDATA(14);
		when "00000000000000001000000000000000" 	=> XS_SDATA_IN0_out <= RDATA(15);
		when "00000000000000010000000000000000" 	=> XS_SDATA_IN0_out <= RDATA(16);
		when "00000000000000100000000000000000" 	=> XS_SDATA_IN0_out <= RDATA(17);
		when "00000000000001000000000000000000" 	=> XS_SDATA_IN0_out <= RDATA(18);
		when "00000000000010000000000000000000" 	=> XS_SDATA_IN0_out <= RDATA(19);
		when "00000000000100000000000000000000" 	=> XS_SDATA_IN0_out <= RDATA(20);
		when "00000000001000000000000000000000" 	=> XS_SDATA_IN0_out <= RDATA(21);
		when "00000000010000000000000000000000" 	=> XS_SDATA_IN0_out <= RDATA(22);
		when "00000000100000000000000000000000" 	=> XS_SDATA_IN0_out <= RDATA(23);
		when "00000001000000000000000000000000" 	=> XS_SDATA_IN0_out <= RDATA(24);
		when "00000010000000000000000000000000" 	=> XS_SDATA_IN0_out <= RDATA(25);
		when "00000100000000000000000000000000" 	=> XS_SDATA_IN0_out <= RDATA(26);
		when "00001000000000000000000000000000" 	=> XS_SDATA_IN0_out <= RDATA(27);
		when "00010000000000000000000000000000" 	=> XS_SDATA_IN0_out <= RDATA(28);
		when "00100000000000000000000000000000" 	=> XS_SDATA_IN0_out <= RDATA(29);
		when "01000000000000000000000000000000" 	=> XS_SDATA_IN0_out <= RDATA(30);
		when "10000000000000000000000000000000" 	=> XS_SDATA_IN0_out <= RDATA(31);
		when others 					=> XS_SDATA_IN0_out <= RDATA(0);
		end case;
	end if;
end if;
end process XS_SDATA_IN0_out_GEN;


LOE_0_GEN: process (I2S_MCLK_GCK3 ,XS_SYNC,XS_SYNC_D1)
begin
if I2S_MCLK_GCK3'event and I2S_MCLK_GCK3 = '1' then
LOE(0) <= not(XS_SYNC) and XS_SYNC_D1;
end if;
end process LOE_0_GEN;

ROE_0_GEN: process (I2S_MCLK_GCK3 ,XS_SYNC,XS_SYNC_D1)
begin
if I2S_MCLK_GCK3'event and I2S_MCLK_GCK3 = '1' then
ROE(0) <= (XS_SYNC) and not(XS_SYNC_D1);
end if;
end process ROE_0_GEN;

LOE_I_GEN: for I in 31 downto 1 generate
process (I2S_MCLK_GCK3 ,XS_BITCLK_out_D1,XS_BITCLK_out,LOE)
begin
if I2S_MCLK_GCK3'event and I2S_MCLK_GCK3 = '1' then
	if (XS_BITCLK_out_D1 = '0' and XS_BITCLK_out = '1') then
 			LOE(I) <= LOE(I-1);
	end if;
end if;
end process;
end generate LOE_I_GEN;


ROE_I_GEN: for I in 31 downto 1 generate
process (I2S_MCLK_GCK3 ,XS_BITCLK_out_D1,XS_BITCLK_out,ROE)
begin
if I2S_MCLK_GCK3'event and I2S_MCLK_GCK3 = '1' then
	if (XS_BITCLK_out_D1 = '0' and XS_BITCLK_out = '1') then
 			ROE(I) <= ROE(I-1);
	end if;
end if;
end process;
end generate ROE_I_GEN;


XS_SYNC_D1_GEN:process (I2S_MCLK_GCK3 ,XS_BITCLK_out,XS_BITCLK_out_D1,XS_SYNC)
begin
if I2S_MCLK_GCK3'event and I2S_MCLK_GCK3 = '1' then
	if (XS_BITCLK_out_D1 = '0' and XS_BITCLK_out = '1') then
 		XS_SYNC_D1 <= XS_SYNC;
	end if;
end if; 
end process XS_SYNC_D1_GEN;


XS_BITCLK_out_GEN: process (I2S_SCLK,I2S_MCLK_GCK3)
begin
if I2S_MCLK_GCK3'event and I2S_MCLK_GCK3 = '1' then
	XS_BITCLK_out  <= not I2S_SCLK;
	XS_BITCLK_out_D1 <= XS_BITCLK_out;
end if;
end process XS_BITCLK_out_GEN;


end Rtl;
