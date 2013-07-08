----------------------------------------------------------------------------
--
--  File:   APP_RBX_CC_top.vhd
--  Rev:    B.0
--  Date:	3-9-04
--  This is the top level VHDL file for the RBX Companion Chip FPGA Code for 
--	the Streetfire Street Racer CPU Card to Application Board Interface
--  Author: Robyn E. Bauer
--
--	History: 
--
--	Updated Directions of I2S Signals, 3-9-04 REB
--	Updated Module RBX_CONMOD to output CLK_I (vs. CLK_O), 3-8-04
--	Removed CLK_3_6_MHz input to preserve global clock buffer, 3-8-04
--	Changed Version Number to B0000001 to reflect addition of SLINK TX/RX FIFOS, 2-19-04
--	Changed FPGA Reset to active High, 2-13-04
--      Modified SLINK SIgnal Names, 2-12-04
--	Converted to App Board Version, REB 1-27-04
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

entity APP_RBX_CC_top is 
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
	XS_SDCKE0	:in	std_logic;	-- SDRAM and/or Sync Static Memory clock enable (SDCKE0) from CPU



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
	XS_BITCLK		:inout std_logic; 	-- GPIO28/AC97 Audio Port or I2S bit clock (input or output) 
	XS_SDATA_IN0	:out	std_logic;	-- GPIO29/AC97 or I2S data Input  (input to cpu)
	XS_SDATA_IN1	:in	std_logic; 	-- GPIO32/AC97 Audio Port data in. I2S sysclk output from cpu
	XS_SDATA_OUT	:in	std_logic; 	-- GPIO30/AC97 Audio Port or I2S data out from cpu
	XS_SYNC		:in	std_logic;	-- GPIO31/AC97 Audio Port or I2S Frame sync signal. (output from cpu)
	XS_nACRESET	:in	std_logic;	-- AC97 Audio Port reset signal. (output from cpu) (NO gpio)

--old definitions prior to 3-9-04
--3-9-04	XS_BITCLK	:out	std_logic; 	-- GPIO28/AC97 Audio Port or I2S bit clock (input or output) 
--3-9-04    XS_SDATA_IN1	:out	std_logic; 	-- GPIO32/AC97 Audio Port data in. (input to cpu)I2S
--3-9-04	XS_SDATA_OUT	:out	std_logic; 	-- GPIO30/AC97 Audio Port or I2S data out from cpu
--3-9-04	XS_SYNC		:out	std_logic;	-- GPIO31/AC97 Audio Port or I2S Frame sync signal. (output from cpu)

-- 11 Extra CPU GPIO also connected to Edge Bus
	-- These function pins must be tristated from XSCALE CPU before they can be used for edge bus output
	-- The Following Signal Names Correspond to Application Board Signal Names

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
	

-- Two (2) Edge Bus Connector Clock inputs (or general purpose inputs) from application board
	I2S_MCLK_GCK3	:in  std_logic; --LOC="C8" GCK3 
	I2S_SCLK_GCK1	:in  std_logic;	--LOC="T8" GCK1 

-- Seventy-nine (79) Edge Bus Connector IO signals to/from Application  Board:

	-- LAN91C111 Ethernet Interface:
	
	ETH_A		:out std_logic_vector(3 downto 1); -- Application Board Address Bus
	ETH_D		:inout std_logic_vector(31 downto 0); -- Application Board Data Bus
	ETH_BE_N		:out std_logic_vector(3 downto 0);
	ETH_CS: OUT std_logic; 
	ETH_ADS_N: OUT std_logic; 
	ETH_RD_N: OUT std_logic; 
	ETH_WR_N: OUT std_logic; 
	ETH_RESET: OUT std_logic; 
	ETH_INT: IN std_logic;
	-- unused outputs to LAN91C111
	ETH_CYCLE_N: OUT std_logic; 
	ETH_DATACS_N: OUT std_logic;
	ETH_RDYRTN_N: OUT std_logic;
	ETH_W_R_N: OUT std_logic; 
	ETH_LCLK: OUT std_logic; 
	ETH_VLBUS_N: OUT std_logic;
	-- unused inputs from LAN91C111
	ETH_LDEV_N: IN std_logic;
	ETH_IOWAIT: IN std_logic; 
		
	--AUDIO PATH CONTROL and I2S SIGNALS:	
	I2S_MCLK : out std_logic;
	I2S_SCLK : inout std_logic;
	I2S_LRCLK : inout std_logic;
	I2S_SDATA : inout std_logic;
	DA_INT_8405A : in std_logic;
	DA_INT_8415A : in std_logic;
	DAI_ENABLE : out std_logic;

--old definitions prior to 3-9-04
--3-9-04	I2S_SCLK : out std_logic;
--3-9-04	I2S_LRCLK : out std_logic;

	--SLINK/IR SIGNALS (remaining, not triple connected):
	SL0_TX: OUT std_logic; 
	SL1_TX: OUT std_logic; 
	SL2_TX: OUT std_logic; 
	SL3_TX: OUT std_logic;
	SL0_RX: IN std_logic; 
	SL1_RX: IN std_logic; 
	SL2_RX: IN std_logic; 
	SL3_RX: IN std_logic;

	--SWITCHES and LEDs:
	SW_1 : in std_logic;
	LED_0 : out std_logic; 
	LED_1 : out std_logic; 
	LED_2 : out std_logic; 
	LED_3 : out std_logic; 

	-- UNUSED (SPARE) FPGA EDGE IO
	FPGA_EB90: out std_logic; 
	FPGA_EB92: out std_logic; 
	FPGA_EB93: out std_logic; 
	FPGA_EB94: out std_logic; 
	FPGA_EB96: out std_logic; 
	FPGA_EB98: out std_logic

	);
end APP_RBX_CC_top;


architecture rtl of APP_RBX_CC_top is

--*******************************************************************************************************
-- VERSION NUMBER:  UPDATE THIS
--
constant VERSION_NUMBER:   bit_vector (31 downto 0) := "10110000000000000000000000000001";
-- make 4 most significant bits = x"B" to indicate app board version of code
-- PERIPHERAL BLOCK CAPABILITY INDICATOR
constant BLOCK_CAPABILITY: bit_vector (31 downto 0) := "00000000000000010000000000111111";
--
--	PB0 - Confguration Peripheral Block (CPB)
--	PB1 - Interrupt Controller Peripheral Block (ICPB)
--	PB2 - SLINK Peripheral Block (SLPB)
--	PB3 - Swith Peripheral Block (SWPB)
--	PB4 - Audio Path Control and I2S Peripheral Block (APCIPB)
--	PB5 - LED Peripheral Block (LEDPB)
--
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
component RBX_SWPB
port(
	RST_I : IN std_logic;
	CLK_I : IN std_logic;
	WE_I : IN std_logic;
	CYC_I : IN std_logic;
	ADR_I : IN std_logic_vector(17 downto 0);
	SEL_I : IN std_logic_vector(3 downto 0);
	DAT_I : IN std_logic_vector(31 downto 0);
	STB_I : IN std_logic;
	PB_EN_I : IN std_logic;
	SW_1 : IN std_logic;          
	DAT_O : OUT std_logic_vector(31 downto 0);
	ACK_O : OUT std_logic;
	PB_INT_O : OUT std_logic
	);
end component;
--++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

--++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
component RBX_APCIPB
port(
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
end component;
--++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


--old RBX_APCIPB (before 3-9-04)
--++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
--component RBX_APCIPB
--port(
--	RST_I : IN std_logic;
--	CLK_I : IN std_logic;
--	WE_I : IN std_logic;
--	CYC_I : IN std_logic;
--	ADR_I : IN std_logic_vector(17 downto 0);
--	SEL_I : IN std_logic_vector(3 downto 0);
--	DAT_I : IN std_logic_vector(31 downto 0);
--	STB_I : IN std_logic;
--	PB_EN_I : IN std_logic;
--	DA_INT_8415A : IN std_logic;
--	DA_INT_8405A : IN std_logic;          
--	DAT_O : OUT std_logic_vector(31 downto 0);
--	ACK_O : OUT std_logic;
--	PB_INT_O : OUT std_logic;
--	DAI_ENABLE : OUT std_logic
--	);
--end component;
--++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

--++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
component rbx_ledpb
port(
	RST_I : IN std_logic;
	CLK_I : IN std_logic;
	WE_I : IN std_logic;
	CYC_I : IN std_logic;
	ADR_I : IN std_logic_vector(17 downto 0);
	SEL_I : IN std_logic_vector(3 downto 0);
	DAT_I : IN std_logic_vector(31 downto 0);
	STB_I : IN std_logic;
	PB_EN_I : IN std_logic;          
	DAT_O : OUT std_logic_vector(31 downto 0);
	ACK_O : OUT std_logic;
	LED_0 : OUT std_logic;
	LED_1 : OUT std_logic;
	LED_2 : OUT std_logic;
	LED_3 : OUT std_logic;
	LED_4 : OUT std_logic
	);
end component;
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

signal CLK_3_6MHz_IBUFG,I2S_SCLK_GCK1_IBUFG,I2S_MCLK_GCK3_IBUFG: std_logic;

--3-12-04 added syn_keep to i2s clocks
attribute syn_keep of I2S_SCLK_GCK1_IBUFG: signal is true;
attribute syn_keep of I2S_MCLK_GCK3_IBUFG: signal is true;


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

signal ETH_INT_O: std_logic;  -- Output from Ethernet Module
signal SL_INT: std_logic;
signal SL_TX	:std_logic_vector(7 downto 0); -- SLINK TX signal
signal SL_RX	:std_logic_vector(7 downto 0); -- SLINK RX signal
signal APCI_INT: std_logic; -- Out from the APCI peripheral block
signal RESET_HIGH: std_logic; -- active high reset
signal SW_INT: std_logic; -- Out from SW peripheral block

signal CLK_3_6MHz:std_logic; -- 3-8-04 (removed CLK_3_6MHz input)

signal XS_SDATA_IN0_out:std_logic;

--**********************************************************
--  LOGIC BEGINS HERE:
--**********************************************************
  
begin

CLK_3_6MHz <= '0'; -- removed input, 3-8-04

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
	EB_GCK3_13 => I2S_MCLK_GCK3,	
	EB_GCK1_147 => I2S_SCLK_GCK1,	
	XS_PWM1 => RESET_HIGH,	
	RST_O => RST_I,		
	CLK_I => CLK_I,
	CLK_3_6MHz_IBUFG => CLK_3_6MHz_IBUFG,
	EB_GCK1_147_IBUFG => I2S_SCLK_GCK1_IBUFG,
	EB_GCK3_13_IBUFG => I2S_MCLK_GCK3_IBUFG );

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
ETH_INT_O	=>	ETH_INT_O,	-- assign ETHPB to PB 16 => use PB_INT(x), x = 16
ETH_ENABLE_I	=>	PB_EN(16),	-- assign ETHPB to PB 16 => use PB_EN(x), x = 16
ETH_ADDR	=>	ETH_A	,	-- address is (3:1)
ETH_DATA	=>	ETH_D	,
ETH_CS		=>	ETH_CS	,
ETH_ADS		=>	ETH_ADS_N	,
ETH_RD		=>	ETH_RD_N	,
ETH_WR		=>	ETH_WR_N	,
ETH_RESET	=>	ETH_RESET,
ETH_INT_I	=>	ETH_INT	,
ETH_BE		=>	ETH_BE_N	,  
E0_CYCLE	=>	ETH_CYCLE_N,
E0_DATACS	=>	ETH_DATACS_N,
E0_RDYRTN	=>	ETH_RDYRTN_N,
E0_W_RN		=>	ETH_W_R_N	,
E0_LCLK		=>	ETH_LCLK	,
E0_LDEV		=>	ETH_LDEV_N	,
E0_IOWAIT	=>	ETH_IOWAIT	,
ETH_VLBUS_N	=>	ETH_VLBUS_N
);

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

--++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
INST_RBX_SWPB: RBX_SWPB
port map(
	RST_I => RST_I,
	CLK_I => CLK_I,
	WE_I => WE_O,
	CYC_I => CYC_O,
	ADR_I => ADR_O,
	SEL_I => SEL_O,
	DAT_I => DAT_O,
	STB_I => PB_STB(3),
	DAT_O => pB_DAT_3_O,
	ACK_O => PB_ACK(3),
	PB_EN_I => PB_EN(3),
	PB_INT_O => SW_INT,
	SW_1 => SW_1
	);
--++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

--++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
INST_RBX_APCIPB: RBX_APCIPB
port map(
	RST_I => RST_I,
	CLK_I => CLK_I,
	WE_I => WE_O,
	CYC_I => CYC_O,
	ADR_I => ADR_O,
	SEL_I => SEL_O,
	DAT_I => DAT_O,
	STB_I => PB_STB(4),
	DAT_O => pB_DAT_4_O,
	ACK_O => PB_ACK(4),
	PB_EN_I => PB_EN(4),
	PB_INT_O => APCI_INT,
	DA_INT_8415A => DA_INT_8415A,
	DA_INT_8405A => DA_INT_8405A,
	DAI_ENABLE => DAI_ENABLE,
	XS_BITCLK => XS_BITCLK ,		
	XS_SDATA_IN0 => XS_SDATA_IN0_out,	
	XS_SDATA_IN1 => XS_SDATA_IN1,	
	XS_SDATA_OUT => XS_SDATA_OUT,	
	XS_SYNC	 => 	XS_SYNC,
	I2S_MCLK_GCK3 => 	I2S_MCLK_GCK3_IBUFG ,
	I2S_SCLK_GCK1 => 	I2S_SCLK_GCK1_IBUFG ,
	I2S_MCLK 	 => 	I2S_MCLK,
	I2S_SCLK 	 => 	I2S_SCLK,
	I2S_LRCLK 	 => 	I2S_LRCLK,
	I2S_SDATA 	 => 	I2S_SDATA
);
XS_SDATA_IN0 <= XS_SDATA_IN0_out;
--++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


--old RBX_APCIPB (before 3-9-04)
--++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
--INST_RBX_APCIPB: RBX_APCIPB
--port map(
--	RST_I => RST_I,
--	CLK_I => CLK_I,
--	WE_I => WE_O,
--	CYC_I => CYC_O,
--	ADR_I => ADR_O,
--	SEL_I => SEL_O,
--	DAT_I => DAT_O,
--	STB_I => PB_STB(4),
--	DAT_O => pB_DAT_4_O,
--	ACK_O => PB_ACK(4),
--	PB_EN_I => PB_EN(4),
--	PB_INT_O => APCI_INT,
--	DA_INT_8415A => DA_INT_8415A,
--	DA_INT_8405A => DA_INT_8405A,
--	DAI_ENABLE => DAI_ENABLE
--);
--++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
-- Connection of APCI I/O:

--++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
INST_RBX_LEDPB: rbx_ledpb 
PORT MAP(
	RST_I => RST_I,
	CLK_I => CLK_I,
	WE_I => WE_O,
	CYC_I => CYC_O,
	ADR_I => ADR_O,
	SEL_I => SEL_O,
	DAT_I => DAT_O,
	STB_I => PB_STB(5),
	DAT_O => pB_DAT_5_O,
	ACK_O => PB_ACK(5),
	PB_EN_I => PB_EN(5),
	LED_0 => LED_0,
	LED_1 => LED_1,
	LED_2 => LED_2,
	LED_3 => LED_3,
	LED_4 => LED_4
);

--++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

-- UNUSED IO:
----------------------------------------------------------
--  APPLICATION BOARD Connected UNUSED IO
----------------------------------------------------------

-- UNUSED RS232 (CONTROLLED BY CPU)
-- FF_CTS (IN) (PRESERVED BELOW)
FF_RTS <= 'Z'; --(OUT)

-- AUDIO CONTROL

-- UNUSED OUPUTS
--3-9-04 I2S_MCLK <= '0';
--3-9-04 I2S_SCLK <= '0';
--3-9-04 I2S_LRCLK <= '0';
--3-9-04 I2S_SDATA <= '0';
--DAI_ENABLE <= '1';

-- SW/LED
-- SW_1 (INPUT - preserved by driving LED)
--LED_0 <= SW_1;
--LED_1 <= not SW_1;
--LED_2 <= SW_1;
--LED_3 <= not SW_1;
--LED_4 <= SW_1;

-- SPARES (all outputs)

--3-15-04 Use spare outputs to monitor I2S Signals

--FPGA_EB90 <= I2S_MCLK_GCK3_IBUFG; -- input from codec
FPGA_EB90 <= XS_BITCLK;
FPGA_EB92 <= I2S_SCLK_GCK1_IBUFG ;  -- input from codec
FPGA_EB93 <= I2S_LRCLK; --input from codec
FPGA_EB94 <= I2S_SDATA; --input from codec
FPGA_EB96 <= XS_SYNC;  -- input from xs
FPGA_EB98 <= XS_SDATA_IN0_out; -- output to xs

----------------------------------------------------------
--   UNUSED IO process to preserve unused inputs
----------------------------------------------------------

UNUSED_IO_GEN: process (RST_I,XS_CS1n,XS_CS2n,XS_SDCKE0,
			XS_nPOE, XS_nPREG,XS_nPIOW,XS_nPIOR,XS_nPCE2,XS_nPCE1,
			XS_nIOIS16,XS_nPWAIT,XS_nPSKTSEL,XS_nACRESET, FF_CTS,
			CLK_3_6MHz_IBUFG
--3-9-04			I2S_MCLK_GCK3_IBUFG,I2S_SCLK_GCK1_IBUFG
)
begin

if RST_I = '1' 
	or not(XS_CS1n = '0' and XS_CS2n = '0') -- will always be true in normal op 
	then
-- set unused outputs connected to CPU to tristate 
	
	XS_DREQ0	<=	 'Z';
	XS_DREQ1	<=	 'Z';
--3-9-04	XS_BITCLK	<=	 'Z';	
--3-9-04	XS_SDATA_IN0	<=	 'Z';
--3-9-04	XS_SDATA_IN1	<=	 'Z';	
--3-9-04	XS_SDATA_OUT	<=	 'Z';
--3-9-04	XS_SYNC		<=	 'Z';	
else
-- set unused outputs to combination including unused inputs 
-- (this will never happen since CS1 and CS2 cannot both be active)
-- OUT <= f(IN,IN)

	if (CLK_3_6MHz_IBUFG 'event and CLK_3_6MHz_IBUFG = '1') then 
	
	XS_DREQ0	<=	XS_nPIOW and XS_nACRESET and XS_SDCKE0 and XS_nPCE2 and XS_nPSKTSEL and FF_CTS;
	XS_DREQ1	<=	XS_nPIOR and XS_nPOE	and	XS_nPREG and XS_nPCE1 and XS_nIOIS16 and XS_nPWAIT  ;

	end if;

--3-9-04	XS_DREQ1	<=	XS_nPIOR and XS_nPOE	and	XS_nPREG	;
--3-9-04	XS_DREQ0	<=	XS_nPIOW and XS_nACRESET and XS_SDCKE0 ;
-- moved remaining unused inputs to remaining unused outputs
--	XS_BITCLK	<=	XS_nPCE2;	--3-8-04 removed CLK_3_6MHz input
--3-9-04	XS_BITCLK	<=	XS_nPCE2	or     CLK_3_6MHz_IBUFG; --3-8-04
--3-9-04	XS_SDATA_IN0	<= XS_nPCE1 ;	
--3-9-04	XS_SDATA_IN1	<= XS_nIOIS16 ;	
--3-8-04 removed CLK_3_6MHz input
--	if (CLK_3_6MHz_IBUFG 'event and CLK_3_6MHz_IBUFG = '1') then 
--3-9-04		XS_DREQ0	<=	XS_nPIOW and XS_nACRESET and XS_SDCKE0 ;
--	end if;
--3-9-04	if (I2S_MCLK_GCK3_IBUFG 'event and I2S_MCLK_GCK3_IBUFG = '1') then 
--3-9-04		XS_SDATA_OUT	<= XS_nPWAIT;
--	end if;
--3-9-04	if (I2S_SCLK_GCK1_IBUFG 'event and I2S_SCLK_GCK1_IBUFG = '1') then 
--3-9-04		XS_SYNC	<= XS_nPSKTSEL and FF_CTS;
--3-9-04	end if;

end if;
end process UNUSED_IO_GEN;
----------------------------------------------------------

----------------------------------------------------------------------
--Interrupt Assignments:
----------------------------------------------------------------------

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
PB_INT(16)<= ETH_INT_O;  -- from instantiation of ethernet module on pb16
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
PB_INT(4) <= APCI_INT;
PB_INT(3) <= SW_INT;
PB_INT(2) <= SL_INT;
PB_INT(1) <= '0';
PB_INT(0) <= '0'; --IR_RX;  -- for test purposes only can remove later
----------------------------------------------------------------------

			
			

end rtl;

