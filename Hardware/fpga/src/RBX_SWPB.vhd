library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

-- 3-8-04 Added sync for intermodule WE and STB signals (to match existing code)

--  Uncomment the following lines to use the declarations that are
--  provided for instantiating Xilinx primitive components.
--library UNISIM;
--use UNISIM.VComponents.all;

entity RBX_SWPB is port
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
	SW_1 : in std_logic -- Switch signal (active low)
);
end RBX_SWPB;

architecture Rtl of RBX_SWPB is

	-- Register Address: 
	constant STAT_REG_ADDR : bit_vector (17 downto 0)  := "000000000000000000"; -- 0x0
	constant MASK_REG_ADDR : bit_vector (17 downto 0)  := "000000000000000100"; -- 0x4
	constant CLEAR_REG_ADDR : bit_vector (17 downto 0) := "000000000000001000"; -- 0x8

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

	signal STAT_REG : std_logic_vector(2 downto 0); -- Status Register
	signal MASK_REG : std_logic_vector(1 downto 0); -- Status Register
	signal CLEAR_REG : std_logic_vector(1 downto 0); -- Status Register

	signal ADR_I_BV : bit_vector(17 downto 0); -- Bit vector version of adr_i for case
	signal MASKED_INT : std_logic_vector(1 downto 0) ; -- Masked interrupt
	signal SW_META : std_logic;
	signal SW_LEVEL: std_logic;
	signal SW_EDGE: std_logic;

	signal WE_I_IN,STB_I_IN:std_logic; --3-7-04 REB Added intermodule sync for WE_I,STB_I

begin

-------------------------------------------------
--3-7-04 Added intermodule sync for WE_I,STB_I
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
	DAT_O_GEN: process (ADR_I_BV, MASK_REG, STAT_REG)
	begin
		case ADR_I_BV is
			when STAT_REG_ADDR =>
				DAT_O(31 downto 3) <= (others => '0'); --added for completeness, reb 3-8-04
				DAT_O(2 downto 0) <= STAT_REG;
			when MASK_REG_ADDR => 
				DAT_O(31 downto 2) <= (others => '0'); --added for completeness, reb 3-8-04
				DAT_O(1 downto 0) <= MASK_REG;
			when others => 
				DAT_O <= (others => '0');
		end case;
	end process DAT_O_GEN;

	-- Generate Mask Register Write
	MASK_REG_GEN: process (RST_I, CLK_I, PB_EN_I, SEL_I, SLAVE_currentState, ADR_I_BV, DAT_I)
	begin
		if (RST_I = '1' or PB_EN_I = '0') then
			MASK_REG <= (others => '0');
		elsif (CLK_I'event and CLK_I = '1') then
			if (SLAVE_currentState = WRITE_STROBE) and (ADR_I_BV = MASK_REG_ADDR) then
				if SEL_I(0) = '1' then
					MASK_REG(1 downto 0) <= DAT_I(1 downto 0);
				end if;
			end if;
		end if;
	end process MASK_REG_GEN;

	-- Generate switch state and edge pulses
	SW_PULSE_GEN: process (PB_EN_I, RST_I, CLK_I, SW_1)
	begin
		if (RST_I = '1' or PB_EN_I = '0') then
			SW_META <= '1';
			SW_LEVEL <= '1';
			SW_EDGE <= '1';
		elsif (CLK_I'event and CLK_I = '1') then
			SW_META <= SW_1;
			SW_LEVEL <= SW_META;
			SW_EDGE <= SW_LEVEL;
		end if;
	end process SW_PULSE_GEN;

	-- Generate switch level status register (Read Only)
	LEVEL_STAT_REG_GEN: process (RST_I, CLK_I, PB_EN_I, SW_LEVEL)
	begin
		if (RST_I = '1' or PB_EN_I = '0') then
			STAT_REG(2) <= '0';
		elsif (CLK_I'event and CLK_I = '1') then
			if (SW_LEVEL = '1') then
				STAT_REG(2) <= '1';
			else
				STAT_REG(2) <= '0';
			end if;
		end if;
	end process LEVEL_STAT_REG_GEN;

	-- Generate switch falling edge status register (Read Only)
	FALLING_STAT_REG_GEN: process (RST_I, CLK_I, PB_EN_I, SW_LEVEL, SW_EDGE)
	begin
		if (RST_I = '1' or PB_EN_I = '0') then
			STAT_REG(0) <= '0';
		elsif (CLK_I'event and CLK_I = '1') then
			if (CLEAR_REG(0) = '1') then
				STAT_REG(0) <= '0';
			elsif (SW_LEVEL = '0' and SW_EDGE = '1') then
				STAT_REG(0) <= '1';
			end if;
		end if;
	end process FALLING_STAT_REG_GEN;

	-- Generate switch falling edge status register (Read Only)
	RISING_STAT_REG_GEN: process (RST_I, CLK_I, PB_EN_I, SW_LEVEL, SW_EDGE)
	begin
		if (RST_I = '1' or PB_EN_I = '0') then
			STAT_REG(1) <= '0';
		elsif (CLK_I'event and CLK_I = '1') then
			if (CLEAR_REG(1) = '1') then
				STAT_REG(1) <= '0';
			elsif (SW_LEVEL = '1' and SW_EDGE = '0') then
				STAT_REG(1) <= '1';
			end if;
		end if;
	end process RISING_STAT_REG_GEN;

	-- Generate clear register 0x08 (Write Only)
	CLEAR_REG_GEN: process(PB_EN_I, RST_I, CLK_I, SEL_I, SLAVE_currentState, ADR_I_BV, DAT_I)
	begin
		if (RST_I = '1' or PB_EN_I = '0') then
			CLEAR_REG <= (others => '0');
		elsif (CLK_I'event and CLK_I = '1') then
			if (SLAVE_currentState = WRITE_STROBE and ADR_I_BV = CLEAR_REG_ADDR) then
				if SEL_I(0) = '1' then
					CLEAR_REG(1 downto 0) <= DAT_I(1 downto 0);
				end if;

--------------------------------------------------------------------------------------
			--3-8-04 added ack state to extend clear pulse, reb
			elsif (SLAVE_currentState = WRITE_ACK) and (ADR_I_BV = CLEAR_REG_ADDR) then
				if SEL_I(0) = '1' then 
					CLEAR_REG(1 downto 0) <= CLEAR_REG(1 downto 0) ;
				end if;
--------------------------------------------------------------------------------------
			else
				CLEAR_REG <= (others => '0');
			end if;
		end if;

	end process CLEAR_REG_GEN;

	-- Generate interrupt output
	MASKED_INT <= MASK_REG(1 downto 0) and STAT_REG(1 downto 0);
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

end Rtl;
