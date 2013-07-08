----------------------------------------------------------------------------
--
--  File:   RBX_ETHERNET.vhd
--  Rev:    1.0.0
--  Date:	3-5-03
--  This is the VHDL module for the MASTER interface for the RBX Companion Chip
--	(FPGA) for Streetfire Street Racer CPU Card to Application Board Interface
--  Author: Robyn E. Bauer
--
--	History:
--
--	Sync'd Intermodule Inputs WE_I,STB_I, 3-5-04	
--	Added ETH_VLBUS_N and reduced ADdress to 3 bits to match App board connections
--	Inverted Byte enables and registered data output (input from eth), REB 12-16-03
--	Added E0_IOWAIT to extend asynch access, REB 12-15-03	
--	Created 12-04-03
----------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;

library unisim;
use unisim.ALL;

entity RBX_ETHERNET is 
	port ( 
-- SYSTEM CONTROL INPUTS
	RST_I		:in std_logic; -- master reset for peripheral bus
	CLK_I		:in std_logic; -- master clock for peripheral bus
-- PERIPHERAL BUS SIGNALS
	WE_I		:in std_logic; -- Write/read indicator for strobe (1 = write)
	CYC_I		:in std_logic; -- cycle indicator
	ADR_I		:in std_logic_vector(17 downto 0); -- peripheral address
	SEL_I		:in std_logic_vector(3 downto 0);  -- peripheral byte selects (active high)
	DAT_I		:in std_logic_vector(31 downto 0); -- peripheral data in to slave 
	STB_I		:in std_logic; -- write/read strobe (active high)
	DAT_O		:out std_logic_vector(31 downto 0);  -- peripheral data out from slave
	ACK_O		:out std_logic; -- peripheral ack (active high)
-- OTHER PERIPHERAL SIGNALS
	ETH_INT_O	:out std_logic; -- output to peripheral interrupt controller
	ETH_ENABLE_I	:in std_logic;  -- input from peripheral configuration block
-- ETHERNET CONTROL SIGNALS (ALWAYS USED)
	ETH_ADDR	:out std_logic_vector(3 downto 1); -- ETHERNET Address Bus
	ETH_DATA	:inout std_logic_vector(31 downto 0); -- ETHERNET Board Data Bus
	ETH_CS: OUT std_logic; 
	ETH_ADS: OUT std_logic; 
	ETH_RD: OUT std_logic; 
	ETH_WR: OUT std_logic; 
	ETH_RESET: OUT std_logic; 
	ETH_INT_I: IN std_logic;
-- ETHERNET BYTE ENABLE SIGNALS (ONLY SUPPORTED ON MODIFIED TEST BOARDS)
	ETH_BE:	OUT std_logic_vector(3 downto 0); -- Ethernet Byte enables (active low- same as XS)
			-- on modified test board only:
			--Ethernet BE0 (R23) --> (R6) EB140 (FPGA-T3) (was E1_IOWAIT)
			--Ethernet BE0 (R24) --> (R7) EB129 (FPGA-M2) (was E1_CS)
			--Ethernet BE2 (R25) --> (R8) EB139 (FPGA-R4) (was E1_INT)
			--Ethernet BE3 (R26) --> (R9) EB136 (FPGA-P1) (was E1_IO16)
-- FIXED ETHERNET OUTPUTS (FIXED OR NOT USED FOR ASYNCHRONOUS ACCESSES)
	E0_CYCLE: OUT std_logic; -- ACTIVE LOW SYNC BURST CTRL - FIX HIGH
	E0_DATACS: OUT std_logic; -- ACTIVE LOW 32 BIT BURST CTRL - FIX HIGH
	E0_RDYRTN: OUT std_logic; -- ACTIVE LOW SYNC READ EXTEND - FIX HIGH
	E0_W_RN: OUT std_logic;   -- SYNC DIRECTION CTRL - LOW= READ - FIX HIGH
	E0_LCLK: OUT std_logic;   -- SYNC CLOCK - TIE HIGH FOR ASYNC MODE - FIX HIGH 
-- OTHER ETHERNET INPUTS 
	E0_LDEV: IN std_logic;
	E0_IOWAIT: IN std_logic;
	ETH_VLBUS_N: out std_logic
	);
end RBX_ETHERNET;


architecture rtl of RBX_ETHERNET is



-- SIGNAL DEFINITIONS:

signal ETH_CS_OUT, ETH_ADS_OUT, ETH_RD_OUT, ETH_WR_OUT: std_logic; 

-- Ethernet Peripheral State Machine

type ENET_StateType is (
ENET_IDLE, 
ENET_ADS,
READ_1,
READ_2,
READ_3,
READ_4,
WRITE_1,
WRITE_2,
WRITE_3,
WRITE_4,
ENET_ACK);

signal ENET_currentState, ENET_nextState : ENET_StateType;

signal WE_I_IN,STB_I_IN:std_logic; --3-5-04 Added intermodule sync for WE_I,STB_I

--**********************************************************
--  LOGIC BEGINS HERE:
--**********************************************************
  
begin

-------------------------------------------------
--3-5-04 Added intermodule sync for WE_I,STB_I
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


--**********************************************************
-- STATE MACHINE
--**********************************************************
-- STATE MACHINE SYNCHRONIZATION 
ENET_currentStateProc: process(RST_I, CLK_I, ENET_nextState,ETH_ENABLE_I) begin
      if (RST_I = '1' or ETH_ENABLE_I = '0') then ENET_currentState <= ENET_IDLE;
       elsif (CLK_I'event and CLK_I = '1') then 
		ENET_currentState <= ENET_nextState;
      end if;
end process ENET_currentStateProc;

-- SLAVE STATE MACHINE TRANSTIONS

ENET_nextStateProc: process(ENET_currentState,STB_I_IN,WE_I_IN) 
begin
case ENET_currentState is

when ENET_IDLE =>
if STB_I_IN = '1' then  ENET_nextState <= ENET_ADS;
end if;

when ENET_ADS =>
if WE_I_IN = '1' then ENET_nextState <= WRITE_1;
elsif WE_I_IN = '0' then ENET_nextState <= READ_1;
end if;

when READ_1 => ENET_nextState <= READ_2;
when READ_2 => ENET_nextState <= READ_3;
when READ_3 => ENET_nextState <= READ_4;
when READ_4 => ENET_nextState <= ENET_ACK; 
when WRITE_1 => ENET_nextState <= WRITE_2;
when WRITE_2 => ENET_nextState <= WRITE_3;
when WRITE_3 => ENET_nextState <= WRITE_4;
when WRITE_4 => ENET_nextState <= ENET_ACK; 
when ENET_ACK =>
if STB_I_IN = '0' then ENET_nextState <=  ENET_IDLE;
end if;
when others => ENET_nextState <=  ENET_IDLE;

end case;
end process ENET_nextStateProc;

--**********************************************************
-- END STATE MACHINE
--**********************************************************

-- State Dependent outputs:

-- ACK_O output: 

ACK_O_GEN: process (ENET_currentState)
begin
case ENET_currentState is
when ENET_ACK 	=> ACK_O <= '1';
when others 	=> ACK_O <= '0';
end case;
end process ACK_O_GEN;

-- ETH_CS output:

ENET_CS_OUTPUT: process(RST_I, CLK_I,ETH_ENABLE_I, ETH_CS_out) begin
      if (RST_I = '1' or ETH_ENABLE_I = '0') then ETH_CS <= '1';
       elsif (CLK_I'event and CLK_I = '1') then 
		ETH_CS <= ETH_CS_out;
      end if;
end process ENET_CS_OUTPUT;

ETH_CS_CTRL:process(ENET_currentState,STB_I) begin
case ENET_currentState is

when ENET_IDLE => ETH_CS_out <= not STB_I; -- assert after STB_I asserts
when ENET_ADS =>  ETH_CS_out <= '0';
when READ_1 =>	ETH_CS_out <= '0';
when READ_2 =>	ETH_CS_out <= '0';
when READ_3 =>	ETH_CS_out <= '0';
when READ_4 =>	ETH_CS_out <= '0';     
when WRITE_1 => ETH_CS_out <= '0';
when WRITE_2 => ETH_CS_out <= '0';
when WRITE_3 => ETH_CS_out <= '0';
when WRITE_4 => ETH_CS_out <= '0'; 
when ENET_ACK => ETH_CS_out <= '0'; 
when others => ETH_CS_out <= '1'; 

end case;
end process ETH_CS_CTRL;

-- ETH_ADS output:

ENET_ADS_OUTPUT: process(RST_I, CLK_I,ETH_ENABLE_I, ETH_ADS_out) begin
      if (RST_I = '1' or ETH_ENABLE_I = '0') then ETH_ADS <= '1';
       elsif (CLK_I'event and CLK_I = '1') then 
		ETH_ADS <= ETH_ADS_out;
      end if;
end process ENET_ADS_OUTPUT;

ETH_ADS_CTRL:process(ENET_currentState,STB_I) begin
case ENET_currentState is

when ENET_IDLE => ETH_ADS_out <= not STB_I; -- assert when STB_I asserts
when others	=> 	ETH_ADS_out <= '1';

end case;
end process ETH_ADS_CTRL;


-- ETH_RD output:
ENET_RD_OUTPUT: process(RST_I, CLK_I,ETH_ENABLE_I, ETH_RD_out) begin
      if (RST_I = '1' or ETH_ENABLE_I = '0') then ETH_RD <= '1';
       elsif (CLK_I'event and CLK_I = '1') then 
		ETH_RD <= ETH_RD_out;
      end if;
end process ENET_RD_OUTPUT;

ETH_RD_CTRL:process(ENET_currentState,WE_I,STB_I) begin
case ENET_currentState is

when ENET_IDLE => ETH_RD_out <= '1';
when ENET_ADS => ETH_RD_out <= WE_I;  -- WE_I = '0' for read
when READ_1 =>	ETH_RD_out <= '0';
when READ_2 =>  ETH_RD_out <= '0';
when READ_3 =>	ETH_RD_out <= '0';
when READ_4 =>	ETH_RD_out <= '0';
when WRITE_1 =>	ETH_RD_out <= '1';
when WRITE_2 =>	ETH_RD_out <= '1';
when WRITE_3 =>	ETH_RD_out <= '1';
when WRITE_4 =>	ETH_RD_out <= '1';
when ENET_ACK => ETH_RD_out <= WE_I or not STB_I;  -- WE_I = '0' for read
when others =>	ETH_RD_out <= '1';

end case;
end process ETH_RD_CTRL;


-- ETH_WR output:
ENET_WR_OUTPUT: process(RST_I, CLK_I,ETH_ENABLE_I, ETH_WR_out) begin
      if (RST_I = '1' or ETH_ENABLE_I = '0') then ETH_WR <= '1';
       elsif (CLK_I'event and CLK_I = '1') then 
		ETH_WR <= ETH_WR_out;
      end if;
end process ENET_WR_OUTPUT;


ETH_WR_CTRL:process(ENET_currentState,WE_I) begin

case ENET_currentState is

when ENET_IDLE => ETH_WR_out <= '1';
when ENET_ADS => ETH_WR_out <= not WE_I;  -- WE_I = '1' for write
when READ_1 =>	ETH_WR_out <= '1';
when READ_2 =>	ETH_WR_out <= '1';
when READ_3 =>	ETH_WR_out <= '1';
when READ_4 =>	ETH_WR_out <= '1';
when WRITE_1 => ETH_WR_out <= '0';
when WRITE_2 => ETH_WR_out <= '0';
when WRITE_3 => ETH_WR_out <= '0';
when WRITE_4 => ETH_WR_out <= '1';
when ENET_ACK => ETH_WR_out <= '1';
when others => ETH_WR_out <= '1';


end case;
end process ETH_WR_CTRL;


-- ETHERNET INTERRUPT output:
ETH_INT_GEN: process(RST_I, CLK_I, ETH_INT_I,ETH_ENABLE_I) begin
      if (RST_I = '1' or ETH_ENABLE_I = '0') then ETH_INT_O <= '0';
       elsif (CLK_I'event and CLK_I = '1') then 
		ETH_INT_O <= ETH_INT_I;
      end if;
end process ETH_INT_GEN;

-- ETHERNET RESET output:
ETH_RESET_GEN: process(RST_I, CLK_I, ETH_ENABLE_I) begin
      if (RST_I = '1') then ETH_RESET <= '0';
       elsif (CLK_I'event and CLK_I = '1') then 
		ETH_RESET <= not ETH_ENABLE_I;
	
      end if;
end process ETH_RESET_GEN;


-- PERIPHERAL DATA BUS output:
-- registered 12-16-03, reb
DATA_OUTPUT_GEN: process (RST_I, CLK_I, ETH_DATA)  begin
if (RST_I = '1') then DAT_O <= (others => '0');
 elsif (CLK_I'event and CLK_I = '1') then DAT_O <= ETH_DATA;
end if;
end process DATA_OUTPUT_GEN;


-- ETHERNET DATA BUS OUTPUT:
-- DATA TRISTATE BUFFER:
ETH_DATA_TRISTATE_OUT: process (CLK_I,DAT_I,ENET_currentState)
begin
if (CLK_I'event and CLK_I = '1') then
case ENET_currentState is
when ENET_IDLE => ETH_DATA <= (others => 'Z');
when ENET_ADS =>  ETH_DATA <= (others => 'Z');
when READ_1 =>    ETH_DATA <= (others => 'Z');
when READ_2 =>    ETH_DATA <= (others => 'Z');
when READ_3 =>    ETH_DATA <= (others => 'Z');
when READ_4 =>    ETH_DATA <= (others => 'Z');
when WRITE_1 =>   ETH_DATA <= DAT_I;
when WRITE_2 =>   ETH_DATA <= DAT_I;
when WRITE_3 =>   ETH_DATA <= DAT_I;
when WRITE_4 =>   ETH_DATA <= DAT_I;
when ENET_ACK =>    ETH_DATA <= (others => 'Z');
when others =>    ETH_DATA <= (others => 'Z');
end case;
end if;
end process ETH_DATA_TRISTATE_OUT;

-- ETHERNET ADDRESS OUTPUT

ETH_ADDR_OUT: process (CLK_I,SEL_I)
begin
if (CLK_I'event and CLK_I = '1') then
ETH_ADDR(1) <= '0';
ETH_ADDR(3 downto 2) <= ADR_I(3 downto 2);

end if;
end process ETH_ADDR_OUT;



-- ETHERNET BYTE ENABLE OUTPUT
ETH_BYTE_ENABLE_OUT: process (CLK_I,SEL_I)
begin
if (CLK_I'event and CLK_I = '1') then
ETH_BE <= not SEL_I;  -- byte enables on ethernet are active low (sel is active hi)
end if;
end process ETH_BYTE_ENABLE_OUT;


--------------------------------------------------------




UNUSED_ETH_IO_GEN: process (RST_I,E0_LDEV,E0_IOWAIT)
begin

if RST_I = '1' -- normal operation
	then
	E0_CYCLE  	<= 'Z';-- ACTIVE LOW SYNC BURST CTRL - pulled up on board
	E0_DATACS 	<= 'Z'; -- ACTIVE LOW 32 BIT BURST CTRL - pulled up on board
	E0_RDYRTN 	<= 'Z'; -- ACTIVE LOW SYNC READ EXTEND - pulled up on board
	E0_W_RN 	<= 'Z';  -- SYNC DIRECTION CTRL - LOW= READ - pulled up on board
	E0_LCLK 	<= 'Z';   -- SYNC CLOCK - TIE HIGH FOR ASYNC MODE - pulled up on board
	ETH_VLBUS_N	<= 'Z';  -- pulled up on board
else  -- DURING RESET PULSE, LET E0_W_RN (NOT VALID) CHANGE TO PRESERVE UNUSED INPUTS
	E0_CYCLE  	<= 'Z';-- ACTIVE LOW SYNC BURST CTRL - pulled up on board
	E0_DATACS 	<= 'Z'; -- ACTIVE LOW 32 BIT BURST CTRL - pulled up on board
	E0_RDYRTN 	<= 'Z'; -- ACTIVE LOW SYNC READ EXTEND - pulled up on board
	E0_W_RN 	<= E0_LDEV or E0_IOWAIT;  -- SYNC DIRECTION CTRL - LOW= READ - pulled up on board
	E0_LCLK 	<= 'Z';   -- SYNC CLOCK - TIE HIGH FOR ASYNC MODE - pulled up on board
	ETH_VLBUS_N	<= 'Z';  -- pulled up on board
end if;
end process UNUSED_ETH_IO_GEN;












end rtl;

