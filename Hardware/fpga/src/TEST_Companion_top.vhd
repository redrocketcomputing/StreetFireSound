----------------------------------------------------------------------------
--
--  File:   TEST_Companion_top.vhd
--  Rev:    A.0
--  Date:	3-6-04
--  This is the top level VHDL file for the RBX Companion Chip FPGA Code for 
--	the Streetfire Street Racer CPU Card to Application Board Interface
--  Author: Robyn E. Bauer
--
--	History: 
--	
--	3-6-04 temporarily removed CLK_3_6MHz input to save global clock buffer
--    3-6-04 updated conmod CLK_O TO CLK_I (signal name)
--	Changed Version Number to A0000001 to reflect addition of SLINK TX/RX FIFOS, 2-19-04
--	Changed FPGA Reset to active High, 2-13-04
--	Incorporated SLINK PB Module, REB 2-9-04
--	Test Interrupts IR_RX PB_INT(0), REB 1-9-04
--	Added RBX_ICPB module (interrupt controller), REB 12-27-03
--	Added RBX_ETHERNET module, REB 12-15-03
--	Tristated all unused outputs, REB 12-8-03	
--	Created 11-24-03
----------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;

library unisim;
use unisim.ALL;

entity TEST_Companion_top is 
	port ( 

-- XSCALE CLOCK INPUTS:
	XS_SDCLK0	:in	std_logic;	--LOC = "B8" ; #GCK2 Synchronous Static Memory clock from CPU
--	CLK_3_6MHz	:in	std_logic;	--LOC = "T9" ; #GCK0 (x-scale 3.6MHz/GPIO[11])

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
	XS_RDnWR		:in	std_logic;	-- CPU Read/Write for static interface
	XS_DQM		:in	std_logic_vector(3 downto 0);	-- Variable Latency IO Write Byte Enables
	XS_RDY		:out	std_logic;	--Variable Latency I/O Ready pin. (input to cpu RDY/GPIO[18])
--	XS_SDCKE0	:in	std_logic;	-- SDRAM and/or Sync Static Memory clock enable (SDCKE0) from CPU
-- XS_SDCKE0 is not used in test code therefore reassigned as tristate output
	XS_SDCKE0	:out	std_logic;	-- SDRAM and/or Sync Static Memory clock enable (SDCKE0) from CPU

-- DMA Requests
	XS_DREQ0	:out	std_logic;	--DMA Request. (input to cpu) or GPIO[20]
	XS_DREQ1	:out	std_logic;	--DMA Request. (input to cpu) or GPIO[19]	

-- FPGA Reset and Interrupt
	XS_PWM1 	:in	std_logic;	--XS GPIO17 -use as fpga reset input (or PWM1 from CPU)
	XS_PWM0		:out	std_logic;	--XS GPIO16 -use as interrupt output (or PWM0 from cpu)

-- PCMCIA/CF Control Signals: (all defined as inputs since not used in fpga yet)
	XS_nPOE		:in std_logic; --PCMCIA Output enable - output from CPU or GPIO48
	XS_nPIOW	:in std_logic; --PCMCIA I/O Write - output from CPU or GPIO51
	XS_nPIOR	:in std_logic; --PCMCIA I/O Read - output from CPU or GPIO50
	XS_nPCE2	:in std_logic; --PCMCIA Card Enable 2- output from CPU or GPIO53
	XS_nPCE1	:in std_logic; --PCMCIA Card Enable 1- output from CPU or GPIO52
	XS_nIOIS16	:in std_logic; --PCMCIA IO Select 16 - input to CPU or GPIO57
	XS_nPWAIT	:in std_logic; --PCMCIA wait - input to cpu or GPIO56
	XS_nPSKTSEL	:in std_logic;  --PCMCIA socket select - output from CPU or GPIO54
	XS_nPREG	:in std_logic; --PCMCIA Register Select - output from CPU or GPIO55

-- AC97/I2S Interface:
	XS_BITCLK	:out	std_logic; 	-- GPIO28/AC97 Audio Port or I2S bit clock (input or output) 
	XS_SDATA_IN0	:out	std_logic;	-- GPIO29/AC97 or I2S data Input  (input to cpu)
	XS_SDATA_IN1	:out	std_logic; 	-- GPIO32/AC97 Audio Port data in. (input to cpu)I2S
	XS_SDATA_OUT	:out	std_logic; 	-- GPIO30/AC97 Audio Port or I2S data out from cpu
	XS_SYNC		:out	std_logic;	-- GPIO31/AC97 Audio Port or I2S Frame sync signal. (output from cpu)
	XS_nACRESET	:in	std_logic;	-- AC97 Audio Port reset signal. (output from cpu) (NO gpio)

-- 11 Extra CPU GPIO also connected to Edge Bus
	-- These function pins must be tristated from XSCALE CPU before they can be used for edge bus output
	-- The Following Signal Names Correspond to Test Board Signal Names

	SL6_TX: OUT std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_FF_DCD/GPIO[36]
	SL5_TX: OUT std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_FF_RI/GPIO[38]
	SL4_TX: OUT std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_SSP_TXD/GPIO[25]
	IR_TX: OUT std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_SSP_RXD/GPIO[26]
	FF_RTS: OUT std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_FF_RTS/gpio41
	LED_4: OUT std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_SSP_SCLK/GPIO[23]

	SL6_RX: IN std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_FF_DTR/GPIO[40]
	SL5_RX: IN std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_FF_DSR/GPIO[37]
	SL4_RX: IN std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_SSPEXTCLK/GPIO[27]
	IR_RX: IN std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_SSP_SFRM/GPIO[24]
	FF_CTS: IN std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_FF_CTS/gpio35
	

-- Two (2) Edge Bus Connector Clock inputs (or general purpose inputs)
	EB_GCK3_13	:in  std_logic; --LOC="C8" GCK3 from edge NOT CONNECTED on test board
	EB_GCK1_147	:in  std_logic;	--LOC="T8" GCK1 from edge (test board I2S_SCLK)

-- Seventy-nine (79) Edge Bus Connector IO signals to/from Test Board:

	-- Test Board Address/Data Bus:
	ADDR		:out std_logic_vector(15 downto 1); -- Test Board Address Bus
	DATA		:inout std_logic_vector(31 downto 0); -- Test Board Data Bus

	-- LAN91C111 Asynchronous Bus Control Signals:	
	E0_CS: OUT std_logic; 
	E0_ADS: OUT std_logic; 
	E0_RD: OUT std_logic; 
	E0_WR: OUT std_logic; 

	-- programmable outputs to LAN91C111/DM9000 
	E1_RESET: OUT std_logic; 

	-- fixed outputs to LAN91C111/DM9000
	E0_CYCLE: OUT std_logic; 
	E0_DATACS: OUT std_logic;
	E0_RDYRTN: OUT std_logic;
	E0_W_RN: OUT std_logic; 
	E0_LCLK: OUT std_logic; 
	
	-- inputs from LAN91C111
	E0_INT: IN std_logic;
	E0_LDEV: IN std_logic;
	E0_IOWAIT: IN std_logic; 
	
	-- DM9000 Ethernet IO
	E1_IOR: OUT std_logic; 
	E1_IOW: OUT std_logic;
	E1_IO32: IN std_logic;
	
	-- $CONFIG$ IO which is different between App/Test Boards:
	-- uncomment one of the following statement groups
---------------------------------------------------------------------------------------------	
	-- APP BOARD/MODIFIED TEST BOARD:
	ETH_BE : out std_logic_vector(3 downto 0);
	-- FPGA PIN T3 IS ETH_BE<0>  Ethernet BE0 (R23) --> (R6) EB140 (FPGA-T3) (was E1_IOWAIT)
	-- FPGA PIN M2 IS ETH_BE<1>  Ethernet BE1 (R24) --> (R7) EB129 (FPGA-M2) (was E1_CS)
	-- FPGA PIN R4 IS ETH_BE<2>  Ethernet BE2 (R25) --> (R8) EB139 (FPGA-R4) (was E1_INT)
	-- FPGA PIN P1 IS ETH_BE<3>  Ethernet BE3 (R26) --> (R9) EB136 (FPGA-P1) (was E1_IO16)
---------------------------------------------------------------------------------------------	
	-- UNMODIFIED TEST BOARD:
--	E1_IOWAIT: IN std_logic;	-- FPGA PIN T3, EB140 on unmodified test board
--	E1_CS: OUT std_logic; 			-- FPGA PIN M2, EB129 on unmodified test board
--	E1_INT: IN std_logic; 			-- FPGA PIN R4, EB139 on unmodified test board
--	E1_IO16: IN std_logic; 		-- FPGA PIN P1, EB136 on unmodified test board
---------------------------------------------------------------------------------------------	
	-- Test Board signals programmable as input or output:
	-- NOTE: if I2S_SCLK or I2S_SDATA are to be output to the test board,
	--       i.e. output enabled in the FPGA tristate control register,
	--	then DAI_ENABLE (GPIO from the CPU) must be driven high to 
	--	turn off the Digital Audio Buffer (74LVC16244) on the Test Board
	I2S_SCLK: inout std_logic;
	I2S_SDATA: inout std_logic; 

	-- Other Test Board Outputs:
	I2S_LRCLK: OUT std_logic;
	I2S_MCLK: OUT std_logic;

	SL0_TX: OUT std_logic; 
	SL1_TX: OUT std_logic; 
	SL2_TX: OUT std_logic; 
	SL3_TX: OUT std_logic;

	-- Other Test Board Inputs:
	SL0_RX: IN std_logic; 
	SL1_RX: IN std_logic; 
	SL2_RX: IN std_logic; 
	SL3_RX: IN std_logic
		);
end TEST_Companion_top;





architecture rtl of TEST_Companion_top is




--*******************************************************************************************************
-- VERSION NUMBER:  UPDATE THIS
--
constant VERSION_NUMBER:   bit_vector (31 downto 0) := "10100000000000000000000000000001";

-- PERIPHERAL BLOCK CAPABILITY INDICATOR
constant BLOCK_CAPABILITY: bit_vector (31 downto 0) := "00000000000000010000000000000111";
--
--	PB0 - Confguration Peripheral Block
--	PB1 - Interrupt Controller Peripheral Block
--	PB2 - SLINK Peripheral Block
--	PB16 - Ethernet Interface (LAN91C111)
--
--*******************************************************************************************************

-----------------------------------------------
--$CONFIG$			
 -- comment out for modified (byte enabled) test board
 -- leave in for unmodified test board with DM9000 
-- signal ETH_BE : std_logic_vector (3 downto 0); 
--------------------------------------------


-- COMPONENT DECLARATIONS:
--++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
component RBX_MASTER_CONNECT
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
end component;


--++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
component RBX_CPB 
	generic(VERSION_NUMBER,BLOCK_CAPABILITY: bit_vector (31 downto 0));
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
	
	PB_EN		:out std_logic_vector(31 downto 0)  -- peripheral block enable signals (active high)

	);
end component;


--++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
component RBX_CONMOD
port(
-- XSCALE CLOCK INPUTS:
	XS_SDCLK0	:in	std_logic;	--LOC = "B8" ; #GCK2 Synchronous Static Memory clock from CPU
	CLK_3_6MHz	:in	std_logic;	--LOC = "T9" ; #GCK0 (x-scale 3.6MHz/GPIO[11])
-- Two (2) Edge Bus Connector Clock inputs (or general purpose inputs)
	EB_GCK3_13	:in  std_logic; --LOC="C8" GCK3 from edge NOT CONNECTED on test board
	EB_GCK1_147	:in  std_logic;	--LOC="T8" GCK1 from edge (test board I2S_SCLK)
-- Reset input from XSCALE
	XS_PWM1 	:in	std_logic;	--XS GPIO17 -use as active low fpga reset input
-- OUTPUTS
	RST_O		:out	std_logic;	-- master reset active high
	CLK_I		:out 	std_logic;	-- master clock
-- EXTRA OUPTUTS (TO GUARANTEE CLOCK BUFFER PLACEMENT)
	CLK_3_6MHz_IBUFG: out std_logic;
	EB_GCK1_147_IBUFG: out std_logic;
	EB_GCK3_13_IBUFG: out std_logic
	);
end component;
--++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
component
RBX_ETHERNET 
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
-- UNUSED ETHERNET INPUTS (NOT USED IN THIS ASYNCHRONOUS INTERFACE)	
	E0_LDEV: IN std_logic;
	E0_IOWAIT: IN std_logic;
	ETH_VLBUS_N: out std_logic
	);

end component; --RBX_ETHERNET;
--++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

component RBX_ICPB
port (

-- SYSTEM CONTROL INPUTS
	RST_I	:in std_logic; -- master reset for peripheral bus
	CLK_I	:in std_logic; -- master clock for peripheral bus

-- PERIPHERAL BUS SIGNALS
	WE_I	:in std_logic; -- Write/read indicator for strobe (1 = write)
	CYC_I	:in std_logic; -- cycle indicator
	ADR_I	:in std_logic_vector(17 downto 0); -- peripheral address
	SEL_I	:in std_logic_vector(3 downto 0);  -- peripheral byte selects (active high)
	DAT_I	:in std_logic_vector(31 downto 0); -- peripheral data in to slave 
	STB_I	:in std_logic; -- write/read strobe (active high)
	DAT_O	:out std_logic_vector(31 downto 0);  -- peripheral data out from slave
	ACK_O	:out std_logic; -- peripheral ack (active high)

-- MODULE SPECIFIC SIGNALS
	PBEN_I	:in std_logic; -- module enable (active high)
	PB_INT	:in std_logic_vector(31 downto 0);  --  peripheral interrupt inputs (pos edge)
	PBINT_O	:out std_logic  -- interrupt output(active high)
	);
end component; --RBX_ICPB;


--++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
component SLINK_PB
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
-- PERIPHERAL MODULE SIGNALS
	PBEN_I		:in std_logic; -- module enable (active high)
	PBINT_O		:out std_logic;  -- interrupt output(active high)
-- SLINK SIGNALS
	SL_TX	:out std_logic_vector(7 downto 0); -- SLINK TX signal
	SL_RX	: in std_logic_vector(7 downto 0) -- SLINK RX signal
	);
end component; --SLINK_PB;
--++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++





--++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
component IBUFG
port (O : out STD_ULOGIC;
I : in STD_ULOGIC);
end component;

--++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

-- SIGNAL DEFINITIONS:

signal RST_I,CLK_I: std_logic;
attribute syn_keep: boolean;
attribute syn_keep of CLK_I: signal is true;

signal CLK_3_6MHz_IBUFG,EB_GCK1_147_IBUFG,EB_GCK3_13_IBUFG: std_logic;

signal WE_O,CYC_O: std_logic;

signal ADR_O: std_logic_vector(17 downto 0);
signal SEL_O: std_logic_vector(3 downto 0);
signal DAT_O: std_logic_vector(31 downto 0);

-- PB0: Configuration Peripheral Block Signals
signal PB_EN: std_logic_vector(31 downto 0);

-- PB1: Interrupt Controller Peripheral Block Signals
signal PB_INT: std_logic_vector(31 downto 0);

-- INTERCONNECT SIGNALS:

-- INTERCONNECT-SLAVE SIGNALS

signal PB_STB: std_logic_vector(31 downto 0);
signal PB_ACK: std_logic_vector(31 downto 0);
signal PB_DAT_31_O,
	PB_DAT_30_O,
	PB_DAT_29_O,
	PB_DAT_28_O,
	PB_DAT_27_O,
	PB_DAT_26_O,
	PB_DAT_25_O,
	PB_DAT_24_O,
	PB_DAT_23_O,
	PB_DAT_22_O,
	PB_DAT_21_O,
	PB_DAT_20_O,
	PB_DAT_19_O,
	PB_DAT_18_O,
	PB_DAT_17_O,
	PB_DAT_16_O,
	PB_DAT_15_O,
	PB_DAT_14_O,
	PB_DAT_13_O,
	PB_DAT_12_O,
	PB_DAT_11_O,
	PB_DAT_10_O,
	PB_DAT_9_O,
	PB_DAT_8_O,
	PB_DAT_7_O,
	PB_DAT_6_O,
	PB_DAT_5_O,
	PB_DAT_4_O,
	PB_DAT_3_O,
	PB_DAT_2_O,
	PB_DAT_1_O,
	PB_DAT_0_O: std_logic_vector(31 downto 0);

signal ETH_INT: std_logic;
signal ETH_VLBUS_N:std_logic;
signal SL_INT: std_logic;
signal SL_TX	:std_logic_vector(7 downto 0); -- SLINK TX signal
signal SL_RX	:std_logic_vector(7 downto 0); -- SLINK RX signal

signal RESET_HIGH:std_logic; -- active high reset

signal CLK_3_6MHz:std_logic;
--**********************************************************
--  LOGIC BEGINS HERE:
--**********************************************************
  
begin

CLK_3_6MHz <= '0';

RESET_HIGH <= not XS_PWM1;

-- COMPONENT INSTANTIATIONS:

--++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
INST_RBX_MASTER_CONNECT: RBX_MASTER_CONNECT PORT MAP(
XS_MD	=>	XS_MD	,
XS_MA	=>	XS_MA	,
XS_CS1n	=>	XS_CS1n	,
XS_CS2n	=>	XS_CS2n	,
XS_CS3n	=>	XS_CS3n	,
XS_CS4n	=>	XS_CS4n	,
XS_CS5n	=>	XS_CS5n	,
XS_nOE	=>	XS_nOE	,
XS_nPWE	=>	XS_nPWE	,
XS_RDnWR =>	XS_RDnWR,
XS_DQM	=>	XS_DQM	,
XS_RDY	=>	XS_RDY	,
RST_I	=>	RST_I	,
CLK_I	=>	CLK_I	,
WE_O	=>	WE_O	,
CYC_O	=>	CYC_O	,
ADR_O	=>	ADR_O	,
SEL_O	=>	SEL_O	,
DAT_O	=>	DAT_O	,

PB_STB		=> 	PB_STB,	
PB_ACK		=> 	PB_ACK,

PB_DAT_31_I	=>	PB_DAT_31_O	,
PB_DAT_30_I	=>	PB_DAT_30_O	,
PB_DAT_29_I	=>	PB_DAT_29_O	,
PB_DAT_28_I	=>	PB_DAT_28_O	,
PB_DAT_27_I	=>	PB_DAT_27_O	,
PB_DAT_26_I	=>	PB_DAT_26_O	,
PB_DAT_25_I	=>	PB_DAT_25_O	,
PB_DAT_24_I	=>	PB_DAT_24_O	,
PB_DAT_23_I	=>	PB_DAT_23_O	,
PB_DAT_22_I	=>	PB_DAT_22_O	,
PB_DAT_21_I	=>	PB_DAT_21_O	,
PB_DAT_20_I	=>	PB_DAT_20_O	,
PB_DAT_19_I	=>	PB_DAT_19_O	,
PB_DAT_18_I	=>	PB_DAT_18_O	,
PB_DAT_17_I	=>	PB_DAT_17_O	,
PB_DAT_16_I	=>	PB_DAT_16_O	,
PB_DAT_15_I	=>	PB_DAT_15_O	,
PB_DAT_14_I	=>	PB_DAT_14_O	,
PB_DAT_13_I	=>	PB_DAT_13_O	,
PB_DAT_12_I	=>	PB_DAT_12_O	,
PB_DAT_11_I	=>	PB_DAT_11_O	,
PB_DAT_10_I	=>	PB_DAT_10_O	,
PB_DAT_9_I	=>	PB_DAT_9_O	,
PB_DAT_8_I	=>	PB_DAT_8_O	,
PB_DAT_7_I	=>	PB_DAT_7_O	,
PB_DAT_6_I	=>	PB_DAT_6_O	,
PB_DAT_5_I	=>	PB_DAT_5_O	,
PB_DAT_4_I	=>	PB_DAT_4_O	,
PB_DAT_3_I	=>	PB_DAT_3_O	,
PB_DAT_2_I	=>	PB_DAT_2_O	,
PB_DAT_1_I	=>	PB_DAT_1_O	,
PB_DAT_0_I	=>	PB_DAT_0_O	
);
--++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
INST_RBX_CONMOD: RBX_CONMOD PORT MAP(

	XS_SDCLK0 => XS_SDCLK0,
	CLK_3_6MHz => CLK_3_6MHz,
	EB_GCK3_13 => EB_GCK3_13,	
	EB_GCK1_147 => EB_GCK1_147,	
	XS_PWM1 => RESET_HIGH,	
	RST_O => RST_I,		
	CLK_I => CLK_I,
	CLK_3_6MHz_IBUFG => CLK_3_6MHz_IBUFG,
	EB_GCK1_147_IBUFG => EB_GCK1_147_IBUFG,
	EB_GCK3_13_IBUFG => EB_GCK3_13_IBUFG );
--++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
INST_RBX_CPB: RBX_CPB

	generic map (VERSION_NUMBER => VERSION_NUMBER,BLOCK_CAPABILITY => BLOCK_CAPABILITY )
	PORT MAP(

	RST_I => RST_I,	
	CLK_I => CLK_I,
	WE_I  => WE_O,
	CYC_I => CYC_O,
	ADR_I => ADR_O,
	SEL_I => SEL_O,
	DAT_I => DAT_O,
	STB_I => PB_STB(0),  -- assign CPB to PB 0 => use PB_STB(x), x = 0
	DAT_O => PB_DAT_0_O, -- assign CPB to PB 0 => use PB_DAT_x_O, x = 0
	ACK_O => PB_ACK(0),  -- assign CPB to PB 0 => use PB_ACK(x), x = 0
	PB_EN => PB_EN	);

--++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
--++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
INST_RBX_ETHERNET: RBX_ETHERNET
PORT MAP( 
RST_I		=>	RST_I	,
CLK_I		=>	CLK_I	,
WE_I		=>	WE_O	,
CYC_I		=>	CYC_O	,
ADR_I		=>	ADR_O	,
SEL_I		=>	SEL_O	,
DAT_I		=>	DAT_O	,
STB_I		=>	PB_STB(16), 	-- assign ETHPB to PB 16 => use PB_STB(x), x = 16
DAT_O		=>	PB_DAT_16_O,	-- assign ETHPB to PB 16 => use PB_DAT_x_O, x = 16
ACK_O		=>	PB_ACK(16),	-- assign ETHPB to PB 16 => use PB_ACK(x), x = 16
--ETH_INT_O	=>	XS_PWM0,	-- assign ETHPB to PB 16 => use PB_INT(x), x = 16
ETH_INT_O	=>	ETH_INT,	-- assign ETHPB to PB 16 => use PB_INT(x), x = 16
ETH_ENABLE_I	=>	PB_EN(16),	-- assign ETHPB to PB 16 => use PB_EN(x), x = 16
ETH_ADDR	=>	ADDR(3 downto 1)	,
ETH_DATA	=>	DATA	,
ETH_CS		=>	E0_CS	,
ETH_ADS		=>	E0_ADS	,
ETH_RD		=>	E0_RD	,
ETH_WR		=>	E0_WR	,
ETH_RESET	=>	E1_RESET,
ETH_INT_I	=>	E0_INT	,
ETH_BE		=>	ETH_BE	,  
E0_CYCLE	=>	E0_CYCLE,
E0_DATACS	=>	E0_DATACS,
E0_RDYRTN	=>	E0_RDYRTN,
E0_W_RN		=>	E0_W_RN	,
E0_LCLK		=>	E0_LCLK	,
E0_LDEV		=>	E0_LDEV	,
E0_IOWAIT	=>	E0_IOWAIT,
ETH_VLBUS_N	=> 	ETH_VLBUS_N	
);
--++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
-- Ethernet Address Connections
ADDR(7 downto 4) <= (others => '0');
ADDR(9 downto 8) <= (others => '1');
ADDR(15 downto 10) <= (others => '0');

--++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
INST_RBX_ICPB: RBX_ICPB
PORT MAP( 
	RST_I	=> RST_I, -- master reset for peripheral bus
	CLK_I	=> CLK_I, -- master clock for peripheral bus
	WE_I	=> WE_O,  -- Write/read indicator for strobe (1 = write)
	CYC_I	=> CYC_O, -- cycle indicator
	ADR_I	=> ADR_O, -- peripheral address
	SEL_I	=> SEL_O, -- peripheral byte selects (active high)
	DAT_I	=> DAT_O, -- peripheral data in to slave 
	STB_I	=> PB_STB(1), -- w/r strobe -- -- assign ETHPB to PB 1 -- use PB_STB(x), x = 1
	DAT_O	=> PB_DAT_1_O, -- peripheral data out from slave --use PB_DAT_x_O, x=1
	ACK_O	=> PB_ACK(1), -- peripheral ack --use PB_ACK(x), x = 1
	PBEN_I	=> PB_EN(1), -- module enable    --use PB_EN(x), x = 1
	PB_INT	=> PB_INT,  --  peripheral interrupt inputs (pos edge)
	PBINT_O	=>	XS_PWM0 -- interrupt output(active high)
	);
--++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++



--++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
INST_SLINK_PB: SLINK_PB
PORT MAP (
	RST_I	=> RST_I, -- master reset for peripheral bus
	CLK_I	=> CLK_I, -- master clock for peripheral bus
	WE_I	=> WE_O,  -- Write/read indicator for strobe (1 = write)
	CYC_I	=> CYC_O, -- cycle indicator
	ADR_I	=> ADR_O, -- peripheral address
	SEL_I	=> SEL_O, -- peripheral byte selects (active high)
	DAT_I	=> DAT_O, -- peripheral data in to slave 
	STB_I  => PB_STB(2),
	DAT_O	=> PB_DAT_2_O,
	ACK_O	=> PB_ACK(2),
	PBEN_I	=> PB_EN(2),
	PBINT_O	=> SL_INT,
	SL_TX	=> SL_TX,
	SL_RX	=> SL_RX
	);
--++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

-- Connection of SLINK I/O:

SL_RX(7) <= IR_RX;
SL_RX(6) <= SL6_RX;
SL_RX(5) <= SL5_RX;
SL_RX(4) <= SL4_RX;
SL_RX(3) <= SL3_RX;
SL_RX(2) <= SL2_RX;
SL_RX(1) <= SL1_RX;
SL_RX(0) <= SL0_RX;

IR_TX <= SL_TX(7);
SL6_TX	<= SL_TX(6);
SL5_TX	<= SL_TX(5);
SL4_TX	<= SL_TX(4);
SL3_TX	<= SL_TX(3);
SL2_TX	<= SL_TX(2);
SL1_TX	<= SL_TX(1);
SL0_TX	<= SL_TX(0);


-- UNUSED IO:

UNUSED_IO_GEN: process (RST_I,XS_CS1n,XS_CS2n,
			EB_GCK3_13_IBUFG,EB_GCK1_147_IBUFG,CLK_3_6MHz_IBUFG,
			XS_nPOE, XS_nPREG,XS_nPIOW,XS_nPIOR,XS_nPCE2,XS_nPCE1,
			XS_nIOIS16,XS_nPWAIT,XS_nPSKTSEL,
			FF_CTS,XS_nACRESET,
			E0_INT,E0_LDEV,E0_IOWAIT,

-----------------------------------------------
--$CONFIG$			
--			E1_INT,E1_IO16,E1_IOWAIT,  -- comment out for byte enabled version
---------------------------------------------


			
			E1_IO32,I2S_SCLK,I2S_SDATA)
begin

if RST_I = '1' 
	or not(XS_CS1n = '0' and XS_CS2n = '0') -- will always be true in normal op 
	then

-- set unused outputs connected to CPU to tristate 
	XS_SDCKE0	<=	 'Z';
	XS_DREQ0	<=	 'Z';
	XS_DREQ1	<=	 'Z';
	XS_BITCLK	<=	 'Z';	
	XS_SDATA_IN0	<=	 'Z';
	XS_SDATA_IN1	<=	 'Z';	
	XS_SDATA_OUT	<=	 'Z';
	XS_SYNC		<=	 'Z';	
	FF_RTS		<=	 'Z';
	LED_4		<=	 'Z';

---------------------------------------------
-- commment out following line for byte enabled test boards:
--$CONFIG$	
--	E1_CS 		<=	 '1';
-------------------------------------------------------------


	E1_IOR		<=	 '1';
	E1_IOW		<=	 '1';

	I2S_SCLK	<=	 'Z';
	I2S_SDATA 	<=	 'Z'; 
	I2S_LRCLK 	<=	 'Z';
	I2S_MCLK	<=	 'Z';
else

-- OUT <= f(IN,IN)
	XS_SDCKE0	<=	XS_nPOE	and	XS_nPREG;
	XS_DREQ0	<=	XS_nPIOW	and	EB_GCK3_13_IBUFG;
	XS_DREQ1	<=	XS_nPIOR	and	EB_GCK1_147_IBUFG;
--	XS_BITCLK	<=	XS_nPCE2	and     CLK_3_6MHz_IBUFG;	
	XS_BITCLK	<=	XS_nPCE2	or     CLK_3_6MHz_IBUFG;	
	XS_SDATA_IN0	<=	XS_nPCE1	;	
	XS_SDATA_IN1	<=	XS_nIOIS16	;	
	XS_SDATA_OUT	<=	XS_nPWAIT	;	
	XS_SYNC		<=	XS_nPSKTSEL	;	
-- OUT <= IN

	FF_RTS	<=	FF_CTS	;
	LED_4	<=	XS_nACRESET	;

---------------------------------------------
-- commment out following line for byte enabled test boards:
-- $CONFIG$	
--	E1_CS <= E1_IOWAIT or E1_INT or E1_IO16;
--------------------------------------------------------------

	E1_IOR	<=	E0_INT or E0_IOWAIT or E1_IO32;
	E1_IOW	<=	E0_LDEV ;

-- INOUT
	I2S_SCLK <= 'Z';
	I2S_SDATA <= 'Z'; 
-- OUT <= INOUT
	I2S_LRCLK <= I2S_SCLK;
	I2S_MCLK <= I2S_SDATA;

end if;


end process UNUSED_IO_GEN;


--Interrupt Assignments:


PB_INT(31) <= '0';
PB_INT(30) <= '0';
PB_INT(29) <= '0';
PB_INT(28) <= '0';
PB_INT(27) <= '0';
PB_INT(26) <= '0';
PB_INT(25) <= '0';
PB_INT(24) <= '0';
PB_INT(23) <= '0';
PB_INT(22) <= '0';
PB_INT(21) <= '0';
PB_INT(20) <= '0';
PB_INT(19) <= '0';
PB_INT(18) <= '0';
PB_INT(17) <= '0';
PB_INT(16)<= ETH_INT;  -- from instantiation of ethernet module on pb16
PB_INT(15) <= '0';
PB_INT(14) <= '0';
PB_INT(13) <= '0';
PB_INT(12) <= '0';
PB_INT(11) <= '0';
PB_INT(10) <= '0';
PB_INT(9) <= '0';
PB_INT(8) <= '0';
PB_INT(7) <= '0';
PB_INT(6) <= '0';
PB_INT(5) <= '0';
PB_INT(4) <= '0';
PB_INT(3) <= '0';
PB_INT(2) <= SL_INT;
PB_INT(1) <= '0';
PB_INT(0) <= IR_RX;  -- for test purposes only can remove later


			
			

end rtl;

