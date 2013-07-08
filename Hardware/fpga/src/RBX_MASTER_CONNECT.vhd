----------------------------------------------------------------------------
--
--  File:   RBX_MASTER_CONNECT.vhd
--  Rev:    1.0.0
--  Date:	3-5-03
--  This is the VHDL module for the MASTER/INTERCONNECT interface for the RBX Companion Chip
--	(FPGA) for Streetfire Street Racer CPU Card to Application Board Interface
--  Author: Robyn E. Bauer
--
--	History: 
--
--	Synchronized PB_ACK input (used in SM), REB 3-5-04
--	Removed END_WE,END_OE states (burst not supported), REB 3-5-04
-- 	Combined RBX_MASTER and RBX_CONNECT modules, 1-8-03
--		added DAT_I input mux, ACK_I input mux, STB_0 output demux
--	Previous history for RBX_MASTER.vhd:
--	Tightened RDY response to CS5,4,3 12-16-03, reb	
--	Created 11-25-03
----------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;

library unisim;
use unisim.ALL;

entity RBX_MASTER_CONNECT is 
	port ( 

-- XSCALE BUS	
	XS_MD		:inout	std_logic_vector(31 downto 0);	-- CPU Memory data (MD) bus
	XS_MA		:in	std_logic_vector(22 downto 0);	-- CPU Memory address (MA) bus
	XS_CS1n		:in	std_logic;	-- CPU Static Chip Select 1 / GPIO[15]
	XS_CS2n		:in	std_logic;	-- CPU Static Chip Select 2 / GPIO[78]
	XS_CS3n		:in	std_logic;	-- CPU Static Chip Select 3 / GPIO[79]
	XS_CS4n		:in	std_logic;	-- CPU Static Chip Select 4 / GPIO[80]
	XS_CS5n		:in	std_logic;	-- CPU Static Chip Select 5 / GPIO[33]
	XS_nOE		:in	std_logic;	-- CPU Memory output enable
	XS_nPWE		:in	std_logic;	-- CPU Memory write enable or GPIO49
	XS_RDnWR	:in	std_logic;	-- CPU Read/Write for static interface
	XS_DQM		:in	std_logic_vector(3 downto 0);	-- active low Write Byte Enables
	XS_RDY		:out	std_logic;	--Variable Latency I/O Ready pin. (input to cpu RDY/GPIO[18])

-- SYSTEM CONTROL INPUTS
	RST_I		:in std_logic; -- master reset for peripheral bus
	CLK_I		:in std_logic; -- master clock for peripheral bus

-- PERIPHERAL BUS SIGNALS
	WE_O		:out std_logic; -- Write/read indicator for strobe (1 = write)
	CYC_O		:out std_logic; -- cycle indicator
	ADR_O		:out std_logic_vector(17 downto 0); -- peripheral address
	SEL_O		:out std_logic_vector(3 downto 0);  -- peripheral byte selects (active high)
	DAT_O		:out std_logic_vector(31 downto 0); -- peripheral data out from master


	PB_STB	:out std_logic_vector(31 downto 0); -- peripheral slave strobe outputs
	PB_ACK	:in std_logic_vector(31 downto 0); -- peripheral slave ack inputs
	PB_DAT_31_I,
	PB_DAT_30_I,
	PB_DAT_29_I,
	PB_DAT_28_I,
	PB_DAT_27_I,
	PB_DAT_26_I,
	PB_DAT_25_I,
	PB_DAT_24_I,
	PB_DAT_23_I,
	PB_DAT_22_I,
	PB_DAT_21_I,
	PB_DAT_20_I,
	PB_DAT_19_I,
	PB_DAT_18_I,
	PB_DAT_17_I,
	PB_DAT_16_I,
	PB_DAT_15_I,
	PB_DAT_14_I,
	PB_DAT_13_I,
	PB_DAT_12_I,
	PB_DAT_11_I,
	PB_DAT_10_I,
	PB_DAT_9_I,
	PB_DAT_8_I,
	PB_DAT_7_I,
	PB_DAT_6_I,
	PB_DAT_5_I,
	PB_DAT_4_I,
	PB_DAT_3_I,
	PB_DAT_2_I,
	PB_DAT_1_I,
	PB_DAT_0_I	:in std_logic_vector(31 downto 0) -- peripheral slave data inputs
	);
end RBX_MASTER_CONNECT;


architecture rtl of RBX_MASTER_CONNECT is

-- SIGNAL DEFINITIONS:

-- signal 	STB_O: std_logic; -- out std_logic; -- write/read strobe (active high)
signal	ACK_I: std_logic; -- in std_logic; -- peripheral ack (active high)

signal 	DAT_I: std_logic_vector(31 downto 0);  -- peripheral data in to master

signal 	PBSEL: std_logic_vector(31 downto 0); -- peripheral block select (active high)

signal SELECTED_ACK_I: std_logic;  -- muxed ACK_I input
signal SELECTED_DAT_I: std_logic_vector(31 downto 0);  -- muxed DAT_I input



signal XS_CS1n_IN,XS_CS2n_IN,XS_CS3n_IN,XS_CS4n_IN,XS_CS5n_IN : std_logic;
signal	XS_nOE_IN,XS_nPWE_IN,XS_RDnWR_IN: std_logic;
signal	XS_DQM_IN: std_logic_vector(3 downto 0);
signal XS_MA_IN: std_logic_vector(22 downto 18);  -- remainder of address registered as ADR_O output
signal RDY_OUT: std_logic;
signal BUS_RDY: std_logic;  --input to rdy output register
signal XS_MD_OUT:std_logic_vector(31 downto 0); -- registered output of data from DAT_I

-- XScale to Peripheral Master State Machine

type MASTER_StateType is (
MASTER_IDLE, 
CYCLE_START,
WRITE_START,
WRITE_STROBE,
WRITE_ACK,
--END_WE,
READ_START,
READ_STROBE,
READ_ACK,
--END_OE,
CYCLE_END);

signal MASTER_currentState, MASTER_nextState : MASTER_StateType;


signal PB_ACK_IN: std_logic_vector(31 downto 0); -- sync'd peripheral slave ack inputs


--**********************************************************
--  LOGIC BEGINS HERE:
--**********************************************************
  
begin

--**********************************************************
-- STATE MACHINE
--**********************************************************
--MASTER STATE MACHINE SYNCHRONIZATION 
MASTER_currentStateProc: process(RST_I, CLK_I, MASTER_nextState) begin
      if (RST_I = '1') then MASTER_currentState <= MASTER_IDLE;
       elsif (CLK_I'event and CLK_I = '1') then 
		MASTER_currentState <= MASTER_nextState;
      end if;
end process MASTER_currentStateProc;

-- MASTER STATE MACHINE TRANSTIONS

MASTER_nextStateProc: process(MASTER_currentState,ACK_I,XS_nOE_IN,XS_nPWE_IN,
	XS_RDnWR_IN,XS_CS5n_IN,XS_CS4n_IN,XS_CS3n_IN) 
begin
case MASTER_currentState is

when MASTER_IDLE =>
	if (XS_CS5n_IN = '0')  or (XS_CS4n_IN = '0')  or (XS_CS3n_IN = '0') 
		
	then MASTER_nextState <= CYCLE_START;
	end if;

when CYCLE_START=>
	if (XS_nOE_IN = '0') then MASTER_nextState <= READ_START;
	elsif (XS_nPWE_IN = '0') then MASTER_nextState <= WRITE_START;
	end if;

when WRITE_START => MASTER_nextState <= WRITE_STROBE;

when WRITE_STROBE =>
	if (ACK_I = '1') then MASTER_nextState <= WRITE_ACK;	
	end if;

when WRITE_ACK =>
	if (XS_CS5n_IN = '1')  and (XS_CS4n_IN = '1')  and (XS_CS3n_IN = '1') 
		
	then MASTER_nextState <= CYCLE_END;
--	elsif XS_nPWE_IN = '1' then MASTER_nextState <= END_WE;--removed 3-5-04
	end if;

--removed END_WE state 3-5-04
--when END_WE =>
--	if (XS_CS5n_IN = '1')  and (XS_CS4n_IN = '1')  and (XS_CS3n_IN = '1') 
--	then MASTER_nextState <= CYCLE_END;
--	elsif XS_nPWE_IN = '0' then MASTER_nextState <= WRITE_START;
--	end if;

when READ_START => MASTER_nextState <= READ_STROBE;

when READ_STROBE =>
	if (ACK_I = '1') then MASTER_nextState <= READ_ACK;	
	end if;
when READ_ACK =>
	if (XS_CS5n_IN = '1')  and (XS_CS4n_IN = '1')  and (XS_CS3n_IN = '1') 
		
	then MASTER_nextState <= CYCLE_END;
--	elsif XS_nOE_IN = '1' then MASTER_nextState <= END_OE;--removed 3-5-04
	end if;

--removed END_OE state 3-5-04
--when END_OE =>
--	if (XS_CS5n_IN = '1')  and (XS_CS4n_IN = '1')  and (XS_CS3n_IN = '1') 
--	then MASTER_nextState <= CYCLE_END;
--	elsif XS_nOE_IN = '0' then MASTER_nextState <= READ_START;
--	end if;

when CYCLE_END =>  MASTER_nextState <=  MASTER_IDLE;

when others => MASTER_nextState <=  MASTER_IDLE;

end case;
end process MASTER_nextStateProc;

--**********************************************************
-- END STATE MACHINE
--**********************************************************

-- READY output: 
RDY_OUTPUT_REG: process (CLK_I,BUS_RDY) 
begin 
if (CLK_I 'event and CLK_I = '1') then RDY_OUT <= BUS_RDY;
end if;
end process RDY_OUTPUT_REG;

-- STATE-DEPENDENT READY LOGIC
BUS_RDY_GEN: process (MASTER_currentState,XS_CS5n, XS_CS4n,XS_CS3n)
begin
case MASTER_currentState is
when MASTER_IDLE => 
	BUS_RDY <= not ( not XS_CS5n or not XS_CS4n or not XS_CS3n) ;
--		BUS_RDY <= '1'; -- 12-16-03 changed to above
when WRITE_ACK => BUS_RDY <= '1';
--when END_WE => BUS_RDY <= '1';--removed 3-5-04
when READ_ACK => BUS_RDY <= '1';
--when END_OE =>  BUS_RDY <= '1';--removed 3-5-04
when CYCLE_END => BUS_RDY <= '1';
when others =>  BUS_RDY <= '0';
end case;
end process BUS_RDY_GEN;

-- RDY TRISTATE BUFFER:
--(only drive output during valid chip select)
RDY_TRISTATE_OUT: process (RST_I,XS_CS5n,XS_CS4n,XS_CS3n,RDY_OUT)
begin
if RST_I = '0' and 
(XS_CS5n = '0' 
or XS_CS4n = '0'
or XS_CS3n = '0')
then XS_RDY <= RDY_OUT;
else XS_RDY <= 'Z';
end if; 
end process RDY_TRISTATE_OUT;


-- Peripheral Bus Signal Outputs (COMBINATIONAL)
--  CYC_O, WE_O, STB_O, SEL_O(3 downto 0), PBSEL(31 downto 0)

PBSEL_GEN: process (XS_CS4n_IN,XS_CS5n_IN,XS_MA_IN(22 downto 18))
begin
if XS_CS4n_IN = '0' and XS_MA_IN(22) = '0' then -- PB 0-15
PBSEL(31 downto 16) <= (others => '0');
PBSEL(15) <=    XS_MA_IN(21) and     XS_MA_IN(20) and     XS_MA_IN(19) and     XS_MA_IN(18); -- "1111"
PBSEL(14) <=    XS_MA_IN(21) and     XS_MA_IN(20) and     XS_MA_IN(19) and not XS_MA_IN(18); -- "1110"
PBSEL(13) <=    XS_MA_IN(21) and     XS_MA_IN(20) and not XS_MA_IN(19) and     XS_MA_IN(18); -- "1101"
PBSEL(12) <=    XS_MA_IN(21) and     XS_MA_IN(20) and not XS_MA_IN(19) and not XS_MA_IN(18); -- "1100"
PBSEL(11) <=    XS_MA_IN(21) and not XS_MA_IN(20) and     XS_MA_IN(19) and     XS_MA_IN(18); -- "1011"
PBSEL(10) <=    XS_MA_IN(21) and not XS_MA_IN(20) and     XS_MA_IN(19) and not XS_MA_IN(18); -- "1010"
PBSEL(9) <=     XS_MA_IN(21) and not XS_MA_IN(20) and not XS_MA_IN(19) and     XS_MA_IN(18); -- "1001"
PBSEL(8) <=     XS_MA_IN(21) and not XS_MA_IN(20) and not XS_MA_IN(19) and not XS_MA_IN(18); -- "1000"
PBSEL(7) <= not XS_MA_IN(21) and     XS_MA_IN(20) and     XS_MA_IN(19) and     XS_MA_IN(18); -- "0111"
PBSEL(6) <= not XS_MA_IN(21) and     XS_MA_IN(20) and     XS_MA_IN(19) and not XS_MA_IN(18); -- "0110"
PBSEL(5) <= not XS_MA_IN(21) and     XS_MA_IN(20) and not XS_MA_IN(19) and     XS_MA_IN(18); -- "0101"
PBSEL(4) <= not XS_MA_IN(21) and     XS_MA_IN(20) and not XS_MA_IN(19) and not XS_MA_IN(18); -- "0100"
PBSEL(3) <= not XS_MA_IN(21) and not XS_MA_IN(20) and     XS_MA_IN(19) and     XS_MA_IN(18); -- "0011"
PBSEL(2) <= not XS_MA_IN(21) and not XS_MA_IN(20) and     XS_MA_IN(19) and not XS_MA_IN(18); -- "0010"
PBSEL(1) <= not XS_MA_IN(21) and not XS_MA_IN(20) and not XS_MA_IN(19) and     XS_MA_IN(18); -- "0001"
PBSEL(0) <= not XS_MA_IN(21) and not XS_MA_IN(20) and not XS_MA_IN(19) and not XS_MA_IN(18); -- "0000"
elsif XS_CS5n_IN = '0'and XS_MA_IN(22) = '0' then -- PB 16-31
PBSEL(15 downto 0) <= (others => '0');
PBSEL(31) <=    XS_MA_IN(21) and     XS_MA_IN(20) and     XS_MA_IN(19) and     XS_MA_IN(18); -- "1111"
PBSEL(30) <=    XS_MA_IN(21) and     XS_MA_IN(20) and     XS_MA_IN(19) and not XS_MA_IN(18); -- "1110"
PBSEL(29) <=    XS_MA_IN(21) and     XS_MA_IN(20) and not XS_MA_IN(19) and     XS_MA_IN(18); -- "1101"
PBSEL(28) <=    XS_MA_IN(21) and     XS_MA_IN(20) and not XS_MA_IN(19) and not XS_MA_IN(18); -- "1100"
PBSEL(27) <=    XS_MA_IN(21) and not XS_MA_IN(20) and     XS_MA_IN(19) and     XS_MA_IN(18); -- "1011"
PBSEL(26) <=    XS_MA_IN(21) and not XS_MA_IN(20) and     XS_MA_IN(19) and not XS_MA_IN(18); -- "1010"
PBSEL(25) <=     XS_MA_IN(21) and not XS_MA_IN(20) and not XS_MA_IN(19) and     XS_MA_IN(18); -- "1001"
PBSEL(24) <=     XS_MA_IN(21) and not XS_MA_IN(20) and not XS_MA_IN(19) and not XS_MA_IN(18); -- "1000"
PBSEL(23) <= not XS_MA_IN(21) and     XS_MA_IN(20) and     XS_MA_IN(19) and     XS_MA_IN(18); -- "0111"
PBSEL(22) <= not XS_MA_IN(21) and     XS_MA_IN(20) and     XS_MA_IN(19) and not XS_MA_IN(18); -- "0110"
PBSEL(21) <= not XS_MA_IN(21) and     XS_MA_IN(20) and not XS_MA_IN(19) and     XS_MA_IN(18); -- "0101"
PBSEL(20) <= not XS_MA_IN(21) and     XS_MA_IN(20) and not XS_MA_IN(19) and not XS_MA_IN(18); -- "0100"
PBSEL(19) <= not XS_MA_IN(21) and not XS_MA_IN(20) and     XS_MA_IN(19) and     XS_MA_IN(18); -- "0011"
PBSEL(18) <= not XS_MA_IN(21) and not XS_MA_IN(20) and     XS_MA_IN(19) and not XS_MA_IN(18); -- "0010"
PBSEL(17) <= not XS_MA_IN(21) and not XS_MA_IN(20) and not XS_MA_IN(19) and     XS_MA_IN(18); -- "0001"
PBSEL(16) <= not XS_MA_IN(21) and not XS_MA_IN(20) and not XS_MA_IN(19) and not XS_MA_IN(18); -- "0000"
else PBSEL <= (others => '0');
end if;
end process PBSEL_GEN;


-- REGISTERED SIGNALS

PERIPH_DATA_OUTPUT_REG: process(CLK_I,XS_MD) 
begin 
if (CLK_I 'event and CLK_I = '1') then DAT_O <= XS_MD;
end if;
end process PERIPH_DATA_OUTPUT_REG;

ADDRESS_OUTPUT_REG: process(CLK_I,XS_MA) 
begin 
if (CLK_I 'event and CLK_I = '1') then ADR_O(17 downto 0) <= XS_MA(17 downto 0);
end if;
end process ADDRESS_OUTPUT_REG;

ADDRESS_INPUT_REG: process (CLK_I,XS_MA) 
begin 
if (CLK_I 'event and CLK_I = '1') then XS_MA_IN(22 downto 18) <= XS_MA(22 downto 18);
end if;
end process ADDRESS_INPUT_REG;

CS1_INPUT_REG: process (CLK_I, XS_CS1n) 
begin 
if (CLK_I 'event and CLK_I = '1' ) then XS_CS1n_IN <= XS_CS1n;
end if;
end process CS1_INPUT_REG;

CS2_INPUT_REG: process (CLK_I, XS_CS2n) 
begin 
if (CLK_I 'event and CLK_I = '1') then XS_CS2n_IN <= XS_CS2n;
end if;
end process CS2_INPUT_REG;

CS3_INPUT_REG: process (CLK_I, XS_CS3n) 
begin 
if (CLK_I 'event and CLK_I = '1') then XS_CS3n_IN <= XS_CS3n;
end if;
end process CS3_INPUT_REG;

CS4_INPUT_REG: process (CLK_I, XS_CS4n) 
begin 
if (CLK_I 'event and CLK_I = '1' ) then XS_CS4n_IN <= XS_CS4n;
end if;
end process CS4_INPUT_REG;

CS5_INPUT_REG: process (CLK_I, XS_CS5n) 
begin 
if (CLK_I 'event and CLK_I = '1') then XS_CS5n_IN <= XS_CS5n;
end if;
end process CS5_INPUT_REG;
	
nOE_INPUT_REG: process (CLK_I, XS_nOE ) 
begin 
if (CLK_I 'event and CLK_I = '1' ) then XS_nOE_IN <= XS_nOE ;
end if;
end process nOE_INPUT_REG;

nPWE_INPUT_REG: process (CLK_I, XS_nPWE ) 
begin 
if (CLK_I 'event and CLK_I = '1') then XS_nPWE_IN <= XS_nPWE ;
end if;
end process nPWE_INPUT_REG;

RDnWR_INPUT_REG: process (CLK_I,XS_RDnWR ) 
begin 
if (CLK_I 'event and CLK_I = '1' ) then XS_RDnWR_IN <= XS_RDnWR ;
end if;
end process RDnWR_INPUT_REG;

DQM_INPUT_REG: process (CLK_I, XS_DQM) 
begin 
if (CLK_I 'event and CLK_I = '1' ) then XS_DQM_IN <= XS_DQM ;
end if;
end process DQM_INPUT_REG;

--%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
-- CPU DATA BUS OUTPUT CONTROL
--%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
-- DATA TRISTATE BUFFER:
DATA_TRISTATE_OUT: process (RST_I, XS_CS5n,XS_CS4n,XS_CS3n,XS_RDnWR,XS_nOE,XS_MD_OUT)
begin
if (RST_I = '1') then XS_MD <= (others => 'Z');
elsif ( XS_RDnWR = '1') and (XS_CS5n = '0' or XS_CS4n = '0' or XS_CS3n = '0' )
then XS_MD <= XS_MD_OUT;
else XS_MD <= (others => 'Z');
end if; 
end process DATA_TRISTATE_OUT;


XS_DATA_OUTPUT: process (CLK_I, DAT_I) 
begin 
if (CLK_I 'event and CLK_I = '1' ) then 
XS_MD_OUT <= DAT_I;
end if;
end process XS_DATA_OUTPUT;

--%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
-- PERIPHERAL BUS INTERCONNECT 
--%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

-- 1-8-04 Added DAT_I input mux, ACK_I input mux, STB_0 output demux

-------------------------------------------------------------------------------
-- DAT_I input mux
-- assignment of DAT_I(31:0) from PB_DAT_x_I(31:0), x = 0,1,...,31
-- latch appropriate data bus into DAT_I during READ STROBE STATE, 
-- leave DAT_I unchanged in other states
-------------------------------------------------------------------------------

DAT_I_INPUT_LATCH: process (RST_I,CLK_I, MASTER_currentState, SELECTED_DAT_I) 
begin 
if (RST_I = '1') then DAT_I <= (others =>'0');
elsif (CLK_I 'event and CLK_I = '1' ) then 
	if (MASTER_currentState = READ_STROBE) then DAT_I <= SELECTED_DAT_I;
	end if;
end if;
end process DAT_I_INPUT_LATCH;


DAT_I_INPUT_MUX: process (PBSEL,
	PB_DAT_31_I,PB_DAT_30_I,PB_DAT_29_I,PB_DAT_28_I,
	PB_DAT_27_I,PB_DAT_26_I,PB_DAT_25_I,PB_DAT_24_I,
	PB_DAT_23_I,PB_DAT_22_I,PB_DAT_21_I,PB_DAT_20_I,
	PB_DAT_19_I,PB_DAT_18_I,PB_DAT_17_I,PB_DAT_16_I,
	PB_DAT_15_I,PB_DAT_14_I,PB_DAT_13_I,PB_DAT_12_I,
	PB_DAT_11_I,PB_DAT_10_I,PB_DAT_9_I,PB_DAT_8_I,
	PB_DAT_7_I,PB_DAT_6_I,PB_DAT_5_I,PB_DAT_4_I,
	PB_DAT_3_I,PB_DAT_2_I,PB_DAT_1_I,PB_DAT_0_I)
 
begin
case PBSEL is 
when "00000000000000000000000000000001" => SELECTED_DAT_I <= PB_DAT_0_I; -- PB0
when "00000000000000000000000000000010" => SELECTED_DAT_I <= PB_DAT_1_I; -- PB1
when "00000000000000000000000000000100" => SELECTED_DAT_I <= PB_DAT_2_I; -- PB2
when "00000000000000000000000000001000" => SELECTED_DAT_I <= PB_DAT_3_I; -- PB3
when "00000000000000000000000000010000" => SELECTED_DAT_I <= PB_DAT_4_I; -- PB4
when "00000000000000000000000000100000" => SELECTED_DAT_I <= PB_DAT_5_I; -- PB5
when "00000000000000000000000001000000" => SELECTED_DAT_I <= PB_DAT_6_I; -- PB6
when "00000000000000000000000010000000" => SELECTED_DAT_I <= PB_DAT_7_I; -- PB7
when "00000000000000000000000100000000" => SELECTED_DAT_I <= PB_DAT_8_I; -- PB8
when "00000000000000000000001000000000" => SELECTED_DAT_I <= PB_DAT_9_I; -- PB9
when "00000000000000000000010000000000" => SELECTED_DAT_I <= PB_DAT_10_I; -- PB10
when "00000000000000000000100000000000" => SELECTED_DAT_I <= PB_DAT_11_I; -- PB11
when "00000000000000000001000000000000" => SELECTED_DAT_I <= PB_DAT_12_I; -- PB12
when "00000000000000000010000000000000" => SELECTED_DAT_I <= PB_DAT_13_I; -- PB13
when "00000000000000000100000000000000" => SELECTED_DAT_I <= PB_DAT_14_I; -- PB14
when "00000000000000001000000000000000" => SELECTED_DAT_I <= PB_DAT_15_I; -- PB15
when "00000000000000010000000000000000" => SELECTED_DAT_I <= PB_DAT_16_I; -- PB16
when "00000000000000100000000000000000" => SELECTED_DAT_I <= PB_DAT_17_I; -- PB17
when "00000000000001000000000000000000" => SELECTED_DAT_I <= PB_DAT_18_I; -- PB18
when "00000000000010000000000000000000" => SELECTED_DAT_I <= PB_DAT_19_I; -- PB19
when "00000000000100000000000000000000" => SELECTED_DAT_I <= PB_DAT_20_I; -- PB20
when "00000000001000000000000000000000" => SELECTED_DAT_I <= PB_DAT_21_I; -- PB21
when "00000000010000000000000000000000" => SELECTED_DAT_I <= PB_DAT_22_I; -- PB22
when "00000000100000000000000000000000" => SELECTED_DAT_I <= PB_DAT_23_I; -- PB23
when "00000001000000000000000000000000" => SELECTED_DAT_I <= PB_DAT_24_I; -- PB24
when "00000010000000000000000000000000" => SELECTED_DAT_I <= PB_DAT_25_I; -- PB25
when "00000100000000000000000000000000" => SELECTED_DAT_I <= PB_DAT_26_I; -- PB26
when "00001000000000000000000000000000" => SELECTED_DAT_I <= PB_DAT_27_I; -- PB27
when "00010000000000000000000000000000" => SELECTED_DAT_I <= PB_DAT_28_I; -- PB28
when "00100000000000000000000000000000" => SELECTED_DAT_I <= PB_DAT_29_I; -- PB29
when "01000000000000000000000000000000" => SELECTED_DAT_I <= PB_DAT_30_I; -- PB30
when "10000000000000000000000000000000" => SELECTED_DAT_I <= PB_DAT_31_I; -- PB31
when others 					=> SELECTED_DAT_I <= x"00000000";  -- arbitrary value for debug purposes
end case;
end process DAT_I_INPUT_MUX;

-------------------------------------------------------------------------------
-- ACK_I input mux/latch
-- assignment of ACK_I from PB_ACK(x), x = 0,1,...,31
-- latch appropriate PB_ACK input to ACK_I during READ STROBE and WRITE STROBE states
-------------------------------------------------------------------------------

ACK_I_INPUT_LATCH: process (RST_I,CLK_I, MASTER_currentState, SELECTED_ACK_I) 
begin 
if (RST_I = '1') then ACK_I <= '0';
elsif (CLK_I 'event and CLK_I = '1' ) then 
	
case MASTER_currentState is
when WRITE_STROBE => ACK_I <= SELECTED_ACK_I;
when READ_STROBE =>  ACK_I <= SELECTED_ACK_I;
when others =>    ACK_I <= '0';
end case;
	
end if;
end process ACK_I_INPUT_LATCH;

-------------------------------------------------
--3-5-04 CHANGED FROM PB_ACK to PB_ACK_IN (sync'd)
-------------------------------------------------
ACK_I_INPUT_MUX: process (PBSEL,PB_ACK_IN) 
begin
case PBSEL is 
when "00000000000000000000000000000001" => SELECTED_ACK_I <= PB_ACK_IN(0); -- PB0
when "00000000000000000000000000000010" => SELECTED_ACK_I <= PB_ACK_IN(1); -- PB1
when "00000000000000000000000000000100" => SELECTED_ACK_I <= PB_ACK_IN(2); -- PB2
when "00000000000000000000000000001000" => SELECTED_ACK_I <= PB_ACK_IN(3); -- PB3
when "00000000000000000000000000010000" => SELECTED_ACK_I <= PB_ACK_IN(4); -- PB4
when "00000000000000000000000000100000" => SELECTED_ACK_I <= PB_ACK_IN(5); -- PB5
when "00000000000000000000000001000000" => SELECTED_ACK_I <= PB_ACK_IN(6); -- PB6
when "00000000000000000000000010000000" => SELECTED_ACK_I <= PB_ACK_IN(7); -- PB7
when "00000000000000000000000100000000" => SELECTED_ACK_I <= PB_ACK_IN(8); -- PB8
when "00000000000000000000001000000000" => SELECTED_ACK_I <= PB_ACK_IN(9); -- PB9
when "00000000000000000000010000000000" => SELECTED_ACK_I <= PB_ACK_IN(10); -- PB10
when "00000000000000000000100000000000" => SELECTED_ACK_I <= PB_ACK_IN(11); -- PB11
when "00000000000000000001000000000000" => SELECTED_ACK_I <= PB_ACK_IN(12); -- PB12
when "00000000000000000010000000000000" => SELECTED_ACK_I <= PB_ACK_IN(13); -- PB13
when "00000000000000000100000000000000" => SELECTED_ACK_I <= PB_ACK_IN(14); -- PB14
when "00000000000000001000000000000000" => SELECTED_ACK_I <= PB_ACK_IN(15); -- PB15
when "00000000000000010000000000000000" => SELECTED_ACK_I <= PB_ACK_IN(16); -- PB16
when "00000000000000100000000000000000" => SELECTED_ACK_I <= PB_ACK_IN(17); -- PB17
when "00000000000001000000000000000000" => SELECTED_ACK_I <= PB_ACK_IN(18); -- PB18
when "00000000000010000000000000000000" => SELECTED_ACK_I <= PB_ACK_IN(19); -- PB19
when "00000000000100000000000000000000" => SELECTED_ACK_I <= PB_ACK_IN(20); -- PB20
when "00000000001000000000000000000000" => SELECTED_ACK_I <= PB_ACK_IN(21); -- PB21
when "00000000010000000000000000000000" => SELECTED_ACK_I <= PB_ACK_IN(22); -- PB22
when "00000000100000000000000000000000" => SELECTED_ACK_I <= PB_ACK_IN(23); -- PB23
when "00000001000000000000000000000000" => SELECTED_ACK_I <= PB_ACK_IN(24); -- PB24
when "00000010000000000000000000000000" => SELECTED_ACK_I <= PB_ACK_IN(25); -- PB25
when "00000100000000000000000000000000" => SELECTED_ACK_I <= PB_ACK_IN(26); -- PB26
when "00001000000000000000000000000000" => SELECTED_ACK_I <= PB_ACK_IN(27); -- PB27
when "00010000000000000000000000000000" => SELECTED_ACK_I <= PB_ACK_IN(28); -- PB28
when "00100000000000000000000000000000" => SELECTED_ACK_I <= PB_ACK_IN(29); -- PB29
when "01000000000000000000000000000000" => SELECTED_ACK_I <= PB_ACK_IN(30); -- PB30
when "10000000000000000000000000000000" => SELECTED_ACK_I <= PB_ACK_IN(31); -- PB31
when others 					=> SELECTED_ACK_I <= '0';
end case;
end process ACK_I_INPUT_MUX;
-------------------------------------------------


-------------------------------------------------
--3-5-04 Added intermodule sync for PB_ACK
-------------------------------------------------
PB_ACK_IN_REG: process (CLK_I,PB_ACK) 
begin 
if (CLK_I 'event and CLK_I = '1') then PB_ACK_IN <= PB_ACK;
end if;
end process PB_ACK_IN_REG;
-------------------------------------------------




-------------------------------------------------------------------------------
-- STB_O output demux
-- assignment of PB_STB(31:0) from STB_O
-- set appropriatge PB_STB(x) after READ START or WRITE START state
-- reset all PB_STB(31:0) on deassertion of XS_CS during READ ACK or WRITE ACK
-------------------------------------------------------------------------------


STB_O_OUTPUT_GEN: 
for I in 31 downto 0 generate 
process (RST_I,CLK_I,MASTER_currentState,PBSEL,XS_CS5n_IN,XS_CS4n_IN,XS_CS3n_IN)
begin
if (RST_I = '1') then	PB_STB(I)  <= '0';
elsif (CLK_I 'event and CLK_I = '1' ) then 
 
	if (MASTER_currentState = READ_START) or (MASTER_currentState = WRITE_START) then PB_STB(I) <= PBSEL(I);
	elsif ((MASTER_currentState = READ_ACK) or (MASTER_currentState = WRITE_ACK) )
			and (XS_CS5n_IN = '1')  and (XS_CS4n_IN = '1')  and (XS_CS3n_IN = '1') 
				then PB_STB(I) <= '0'; -- clear on CPU deassertion of CS during read ack or write ack
	elsif ((MASTER_currentState = CYCLE_START) or (MASTER_currentState = MASTER_IDLE) or (MASTER_currentState = CYCLE_END))
				then  PB_STB(I) <= '0'; -- keep clear on other states
	end if;

end if;
end process;
end generate STB_O_OUTPUT_GEN;

WE_O_GEN: process (RST_I,CLK_I,MASTER_currentState,XS_CS5n_IN,XS_CS4n_IN,XS_CS3n_IN)
begin
if (RST_I = '1') then	WE_O <= '0';
elsif (CLK_I 'event and CLK_I = '1' ) then 
 
	if (MASTER_currentState = WRITE_START) then WE_O <= '1';
	elsif  (MASTER_currentState = WRITE_ACK) 
			and (XS_CS5n_IN = '1')  and (XS_CS4n_IN = '1')  and (XS_CS3n_IN = '1') 
				then WE_O <= '0'; -- clear on CPU deassertion of CS during write ack
	elsif ((MASTER_currentState = CYCLE_START) or (MASTER_currentState = MASTER_IDLE) 
		or (MASTER_currentState = CYCLE_END) or (MASTER_currentState = READ_START)
		or (MASTER_currentState = READ_ACK) )
				then  WE_O <= '0'; -- keep clear on other states
	end if;

end if;
end process WE_O_GEN;




SEL_O_GEN: process (XS_DQM_IN)
begin
SEL_O <= not XS_DQM_IN;
end process SEL_O_GEN;


CYC_O_GEN: process (MASTER_currentState)
begin
case MASTER_currentState is
when WRITE_START => CYC_O <= '1';
when WRITE_STROBE => CYC_O <= '1';
when WRITE_ACK => CYC_O <= '1';
--when END_WE	=> CYC_O <= '1';--removed 3-5-04
when READ_START => CYC_O <= '1';
when READ_STROBE => CYC_O <= '1';
when READ_ACK =>  CYC_O <= '1';
--when END_OE	=> CYC_O <= '1';--removed 3-5-04
when others =>  CYC_O <= '0';
end case;
end process CYC_O_GEN;




end rtl;

