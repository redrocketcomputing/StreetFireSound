library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

-- 3-7-04 REB, Added intermodule sync of WE and STB inputs (to match existing code)

--  Uncomment the following lines to use the declarations that are
--  provided for instantiating Xilinx primitive components.
--library UNISIM;
--use UNISIM.VComponents.all;

entity RBX_LEDPB is port
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
--	PB_INT_O : out std_logic; -- Peripheral block interrupt output (active high)

	-- External Signals
	LED_0 : out std_logic; -- LED 0 signal (active high)
	LED_1 : out std_logic; -- LED 1 signal (active high)
	LED_2 : out std_logic; -- LED 2 signal (active high)
	LED_3 : out std_logic; -- LED 3 signal (active high)
	LED_4 : out std_logic -- LED 4 signal (active high)
);
end RBX_LEDPB;

architecture Rtl of RBX_LEDPB is

	-- Register Address: 
	constant CNTL_REG_ADDR : bit_vector (17 downto 0) := "000000000000000000"; -- 0x0

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
	signal CNTL_REG : std_logic_vector(4 downto 0); -- Control Register
	signal ADR_I_BV : bit_vector(17 downto 0); -- Bit vector version of adr_i for case

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
	DAT_O_GEN: process (ADR_I_BV, CNTL_REG)
	begin
		DAT_O(31 downto 5) <= (others => '0');
		DAT_O(4 downto 0) <= CNTL_REG(4 downto 0);
	end process DAT_O_GEN;

	-- Generate Control Register Write
	CNTL_REG_GEN: process (RST_I, CLK_I, PB_EN_I, SEL_I, SLAVE_currentState, ADR_I_BV, DAT_I)
	begin
		if (RST_I = '1' or PB_EN_I = '0') then
			CNTL_REG <= (others => '0');
		elsif (CLK_I'event and CLK_I = '1') then
			if (SLAVE_currentState = WRITE_STROBE) and (ADR_I_BV = CNTL_REG_ADDR) then
				if SEL_I(0) = '1' then
					CNTL_REG(4 downto 0) <= DAT_I(4 downto 0);
				end if;
			end if;
		end if;
	end process CNTL_REG_GEN;

	-- Generate LED 0 output
	LED_0_OUTPUT_GEN: process (RST_I, CLK_I, PB_EN_I, CNTL_REG)
	begin
		if (RST_I = '1' or PB_EN_I = '0') then
			LED_0 <= '0';
		elsif (CLK_I'event and CLK_I = '1') then
			if (CNTL_REG(0) = '1') then
				LED_0 <= '1';
			else
				LED_0 <= '0';
			end if;
		end if;
	end process LED_0_OUTPUT_GEN;

	-- Generate LED 1 output
	LED_1_OUTPUT_GEN: process (RST_I, CLK_I, PB_EN_I, CNTL_REG)
	begin
		if (RST_I = '1' or PB_EN_I = '0') then
			LED_1 <= '0';
		elsif (CLK_I'event and CLK_I = '1') then
			if (CNTL_REG(1) = '1') then
				LED_1 <= '1';
			else
				LED_1 <= '0';
			end if;
		end if;
	end process LED_1_OUTPUT_GEN;

	-- Generate LED 2 output
	LED_2_OUTPUT_GEN: process (RST_I, CLK_I, PB_EN_I, CNTL_REG)
	begin
		if (RST_I = '1' or PB_EN_I = '0') then
			LED_2 <= '0';
		elsif (CLK_I'event and CLK_I = '1') then
			if (CNTL_REG(2) = '1') then
				LED_2 <= '1';
			else
				LED_2 <= '0';
			end if;
		end if;
	end process LED_2_OUTPUT_GEN;

	-- Generate LED 3 output
	LED_3_OUTPUT_GEN: process (RST_I, CLK_I, PB_EN_I, CNTL_REG)
	begin
		if (RST_I = '1' or PB_EN_I = '0') then
			LED_3 <= '0';
		elsif (CLK_I'event and CLK_I = '1') then
			if (CNTL_REG(3) = '1') then
				LED_3 <= '1';
			else
				LED_3 <= '0';
			end if;
		end if;
	end process LED_3_OUTPUT_GEN;

	-- Generate LED 4 output
	LED_4_OUTPUT_GEN: process (RST_I, CLK_I, PB_EN_I, CNTL_REG)
	begin
		if (RST_I = '1' or PB_EN_I = '0') then
			LED_4 <= '0';
		elsif (CLK_I'event and CLK_I = '1') then
			if (CNTL_REG(4) = '1') then
				LED_4 <= '1';
			else
				LED_4 <= '0';
			end if;
		end if;
	end process LED_4_OUTPUT_GEN;
end Rtl;
