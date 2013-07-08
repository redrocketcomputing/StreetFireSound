--------------------------------------------------------
-- FILE: APP_RBX_CC_TB.vhd 
--
-- Date: 2-12-04
-- Author: Robyn Bauer
-- Purpose: Streetfire StreetRacer CPU Card to RBX Companion Chip (FPGA) Verification--
-- Note: This module is NOT syntheziable! It if for simulation only !!!
--
-- Revision: 0.0
--
-- History:  
--
--	3-8-04 Moved SLINK loopback to stim file
--	3-8-04 removed CLK_3_6MHz input from top level to preserve global clock buffer
--	Converted to App Board Version, 1-27-04, REB
--	-> initial draft 11-25-03
--	Modified SLINK Signal Names 2-12-04
--	Added SLINK Loopback SL_RX(k) <= SL_TX(k), k=0,1,...,6 and IR_RX <= IR_TX, 2-12-04, REB
--	(Need to break IR_RX/IR_TX loop to run previous interrupt verification test)
--
-- 
--
-- -------------------------------------------------------

LIBRARY ieee;
USE ieee.std_logic_1164.ALL;
USE ieee.numeric_std.ALL;

library modelsim_lib;   
use modelsim_lib.util.all;

ENTITY APP_RBX_CC_TB is				
END APP_RBX_CC_TB;

ARCHITECTURE functional_tst of APP_RBX_CC_TB is


--Signals
signal top_FAIL_FLAG:std_logic;
constant FAIL_FLAG_path: string:= "/APP_RBX_CC_tb/tb_APP_RBX_CC_stim/fail_flag";

signal XS_MD:std_logic_vector(31 downto 0);	-- CPU Memory data (MD) bus
signal XS_MA:std_logic_vector(22 downto 0);	-- CPU Memory address (MA) bus
signal XS_CS1n:std_logic;	-- CPU Static Chip Select 1 / GPIO[15]
signal XS_CS2n:std_logic;	-- CPU Static Chip Select 2 / GPIO[78]
signal XS_CS3n:std_logic;	-- CPU Static Chip Select 3 / GPIO[79]
signal XS_CS4n:std_logic;	-- CPU Static Chip Select 4 / GPIO[80]
signal XS_CS5n:std_logic;	-- CPU Static Chip Select 5 / GPIO[33]
signal XS_nOE:std_logic;	-- CPU Memory output enable
signal XS_nPWE:std_logic;	-- CPU Memory write enable or GPIO49
signal XS_RDnWR:std_logic;	-- CPU Read/Write for static interface
signal XS_DQM:std_logic_vector(3 downto 0);	-- Variable Latency IO Write Byte Enables
signal XS_RDY:std_logic;	--Variable Latency I/O Ready pin. (input to cpu RDY/GPIO[18])
signal XS_PWM1:std_logic;	--XS GPIO17 -use as fpga reset input (or PWM1 from CPU)
signal XS_PWM0:std_logic;	--XS GPIO16 -use as interrupt output (or PWM0 from cpu)
signal XS_SDCLK0:std_logic;	-- Synchronous Static Memory clock from CPU

signal	SL0_TX: std_logic; 
signal	SL1_TX: std_logic; 
signal	SL2_TX: std_logic; 
signal	SL3_TX: std_logic;
signal 	SL4_TX: std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_SSP_TXD/GPIO[25]
signal 	SL5_TX: std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_FF_RI/GPIO[38]
signal 	SL6_TX: std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_FF_DCD/GPIO[36]
signal 	IR_TX: std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_SSP_RXD/GPIO[26]
signal	SL0_RX: std_logic; 
signal	SL1_RX: std_logic; 
signal	SL2_RX: std_logic; 
signal	SL3_RX: std_logic;
signal 	SL4_RX: std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_SSPEXTCLK/GPIO[27]
signal 	SL5_RX: std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_FF_DSR/GPIO[37]
signal 	SL6_RX: std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_FF_DTR/GPIO[40]
signal 	IR_RX:  std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_SSP_SFRM/GPIO[24]

signal 	ETH_A		:std_logic_vector(3 downto 1); -- Test Board Address Bus
signal 	ETH_D		:std_logic_vector(31 downto 0); -- Test Board Data Bus
signal  ETH_BE_N	:std_logic_vector(3 downto 0);
signal  ETH_CS: std_logic; 
signal	ETH_ADS_N: std_logic; 
signal	ETH_RD_N: std_logic; 
signal	ETH_WR_N: std_logic; 
signal	ETH_RESET: std_logic; 
signal	ETH_INT: std_logic;

signal XS_SDCKE0:std_logic;	-- SDRAM and/or Sync Static Memory clock enable (SDCKE0) from CPU
signal CLK_3_6MHz:std_logic;	--3.6 MHz Processor Clock output from CPU Pin A7 (3.6MHz/GPIO[11])
signal XS_DREQ0:std_logic;	--DMA Request. (input to cpu) or GPIO[20]
signal XS_DREQ1:std_logic;	--DMA Request. (input to cpu) or GPIO[19]	
signal XS_nPOE:std_logic; --PCMCIA Output enable - output from CPU or GPIO48
signal XS_nPIOW:std_logic; --PCMCIA I/O Write - output from CPU or GPIO51
signal XS_nPIOR:std_logic; --PCMCIA I/O Read - output from CPU or GPIO50
signal XS_nPCE2:std_logic; --PCMCIA Card Enable 2- output from CPU or GPIO53
signal XS_nPCE1:std_logic; --PCMCIA Card Enable 1- output from CPU or GPIO52
signal XS_nIOIS16:std_logic; --PCMCIA IO Select 16 - input to CPU or GPIO57
signal XS_nPWAIT:std_logic; --PCMCIA wait - input to cpu or GPIO56
signal XS_nPSKTSEL:std_logic;  --PCMCIA socket select - output from CPU or GPIO54
signal XS_nPREG:std_logic; --PCMCIA Register Select - output from CPU or GPIO55
signal XS_BITCLK	:std_logic; 	-- AC97 Audio Port or I2S bit clock (input or output) 
signal XS_SDATA_IN0	:std_logic;	--AC97 or I2S data Input  (input to cpu)
signal XS_SDATA_IN1	:std_logic; 	--AC97 Audio Port data in. (input to cpu)I2S
signal XS_SDATA_OUT	:std_logic; 	--AC97 Audio Port or I2S data out from cpu
signal XS_SYNC		:std_logic;	--AC97 Audio Port or I2S Frame sync signal. (output from cpu)
signal XS_nACRESET	:std_logic;	--AC97 Audio Port reset signal. (output from cpu)


signal 	FF_RTS: std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_FF_RTS/gpio41
signal 	LED_4: std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_SSP_SCLK/GPIO[23]


signal 	FF_CTS: std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_FF_CTS/gpio35
signal 	I2S_MCLK_GCK3	:std_logic; --LOC="C8" GCK3 from edge NOT CONNECTED on test board
signal 	I2S_SCLK_GCK1	:std_logic;	--LOC="T8" GCK1 from edge (test board I2S_SCLK)
signal	ETH_CYCLE_N: std_logic; 
signal	ETH_DATACS_N: std_logic;
signal	ETH_RDYRTN_N: std_logic;
signal	ETH_W_R_N: std_logic; 
signal	ETH_LCLK: std_logic; 
signal	ETH_VLBUS_N: std_logic;
signal	ETH_LDEV_N: std_logic;
signal	ETH_IOWAIT: std_logic; 
signal	I2S_MCLK : std_logic;
signal	I2S_SCLK : std_logic;
signal	I2S_LRCLK : std_logic;
signal	I2S_SDATA : std_logic;
signal	DA_INT_8405A : std_logic;
signal	DA_INT_8415A : std_logic;
signal	DAI_ENABLE : std_logic;

signal	SW_1 : std_logic;
signal	LED_0 : std_logic; 
signal	LED_1 : std_logic; 
signal	LED_2 : std_logic; 
signal	LED_3 : std_logic; 
signal	FPGA_EB90: std_logic; 
signal	FPGA_EB92: std_logic; 
signal	FPGA_EB93: std_logic; 
signal	FPGA_EB94: std_logic; 
signal	FPGA_EB96: std_logic; 
signal	FPGA_EB98: std_logic;


--******************************************************************************************
--signals to spy on:
--signal top_CFI_WAIT_N:std_logic;
--constant CFI_WAIT_N_path: string:= "/ls_tb/uut/digilab_cpld/cfi_wait_n";
-- end of signals to spy on:  See init_signal_spy procedure calls below:
--******************************************************************************************


-- COMPONENT DEFINITIONS:

--++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	COMPONENT APP_RBX_CC_top 
    Port (
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
END COMPONENT; -- APP_RBX_CC_top 
--******************************************************************************************

--******************************************************************************************
	COMPONENT APP_RBX_CC_STIM
	Port(	
	XS_MD		: inout	std_logic_vector(31 downto 0);-- data (MD) bus
	XS_MA		:out	std_logic_vector(22 downto 0);	-- address (MA) bus
	XS_CS1n	:out	std_logic;	-- CPU Static Chip Select 1 / GPIO[15]
	XS_CS2n	:out	std_logic;	-- CPU Static Chip Select 2 / GPIO[78]
	XS_CS3n	:out	std_logic;	-- CPU Static Chip Select 3 / GPIO[79]
	XS_CS4n	:out	std_logic;	-- CPU Static Chip Select 4 / GPIO[80]
	XS_CS5n	:out	std_logic;	-- CPU Static Chip Select 5 / GPIO[33]
	XS_nOE	:out	std_logic;	-- CPU Memory output enable
	XS_nPWE	:out	std_logic;	-- CPU Memory write enable or GPIO49
	XS_RDnWR	:out	std_logic;	-- CPU Read/Write for static interface
	XS_DQM	:out	std_logic_vector(3 downto 0);	-- Var Lat IO Wr Byte En
	XS_RDY	:in	std_logic;	--Variable Latency I/O Ready pin. (input to cpu RDY/GPIO[18])
	XS_SDCLK0	:out	std_logic;	-- Synchronous Static Memory clock from CPU
	XS_PWM1 	:out	std_logic;	--XS GPIO17 -use as fpga reset input (or PWM1 from CPU)
	XS_PWM0	:in	std_logic;	--XS GPIO16 -use as interrupt output (or PWM0 from cpu)
	XS_SDCKE0	:out std_logic;	-- SDRAM and/or Sync Static Memory clock enable (SDCKE0) from CPU
	CLK_3_6MHz	:out std_logic;	--3.6 MHz Processor Clock output from CPU Pin A7 (3.6MHz/GPIO[11])
	XS_nIOIS16	:out	std_logic;
	XS_nPWAIT	:out	std_logic;
	XS_nPREG	:out	std_logic;
	XS_nPSKTSEL	:out	std_logic;
	XS_nPCE2	:out	std_logic;
	XS_nPCE1	:out	std_logic;
	XS_nPIOW	:out	std_logic;
	XS_nPIOR	:out	std_logic;
	XS_nPOE		:out	std_logic;
	XS_nACRESET	:out	std_logic;
	XS_DREQ0, 	
	XS_DREQ1:in std_logic;
	XS_BITCLK		:inout std_logic; 	-- GPIO28/AC97 Audio Port or I2S bit clock (input or output) 
	XS_SDATA_IN0	:in	std_logic;	-- GPIO29/AC97 or I2S data Input  (input to cpu)
	XS_SDATA_IN1	:out	std_logic; 	-- GPIO32/AC97 Audio Port data in. I2S sysclk output from cpu
	XS_SDATA_OUT	:out	std_logic; 	-- GPIO30/AC97 Audio Port or I2S data out from cpu
	XS_SYNC		:out	std_logic;	-- GPIO31/AC97 Audio Port or I2S Frame sync signal. (output from cpu)
	SL6_TX: in std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_FF_DCD/GPIO[36]
	SL5_TX: in std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_FF_RI/GPIO[38]
	SL4_TX: in std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_SSP_TXD/GPIO[25]
	IR_TX: in std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_SSP_RXD/GPIO[26]
	FF_RTS: in std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_FF_RTS/gpio41
	LED_4: in std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_SSP_SCLK/GPIO[23]
	SL6_RX: out std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_FF_DTR/GPIO[40]
	SL5_RX: out std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_FF_DSR/GPIO[37]
	SL4_RX: out std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_SSPEXTCLK/GPIO[27]
	IR_RX: out std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_SSP_SFRM/GPIO[24]
	FF_CTS: out std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_FF_CTS/gpio35
	I2S_MCLK_GCK3	:out  std_logic; --LOC="C8" GCK3 
	I2S_SCLK_GCK1	:out  std_logic;	--LOC="T8" GCK1 
	ETH_INT: out std_logic;
	ETH_CYCLE_N: in std_logic; 
	ETH_DATACS_N: in std_logic;
	ETH_RDYRTN_N: in std_logic;
	ETH_W_R_N: in std_logic; 
	ETH_LCLK: in std_logic; 
	ETH_VLBUS_N: in std_logic;
	ETH_LDEV_N: out std_logic;
	ETH_IOWAIT: out std_logic; 
	I2S_MCLK : in std_logic;
	I2S_SCLK : inout std_logic;
	I2S_LRCLK : inout std_logic;
	I2S_SDATA : inout std_logic;
	DA_INT_8405A : out std_logic;
	DA_INT_8415A : out std_logic;
	DAI_ENABLE : in std_logic;
	SL0_TX: in std_logic; 
	SL1_TX: in std_logic; 
	SL2_TX: in std_logic; 
	SL3_TX: in std_logic;
	SL0_RX: out std_logic; 
	SL1_RX: out std_logic; 
	SL2_RX: out std_logic; 
	SL3_RX: out std_logic;
	SW_1 : out std_logic;
	LED_0 : in std_logic; 
	LED_1 : in std_logic; 
	LED_2 : in std_logic; 
	LED_3 : in std_logic; 
	FPGA_EB90: in std_logic; 
	FPGA_EB92: in std_logic; 
	FPGA_EB93: in std_logic; 
	FPGA_EB94: in std_logic; 
	FPGA_EB96: in std_logic; 
	FPGA_EB98: in std_logic
	);	
END COMPONENT; -- APP_RBX_CC_STIM 


COMPONENT ETH_TARGET 
    Port (
	SIM_CLK: in std_logic;
	DATA	: inout std_logic_vector (31 downto 0); 
	ADDR	: in	std_logic_vector (3 downto 1); 
			
	E0_CS	: in   std_logic; 
	E0_ADS  : in   std_logic;
	E0_RD	: in	std_logic;
	E0_WR	: in	std_logic;
	E1_RESET: in 	std_logic

);	
END COMPONENT; -- ETH_TARGET 





--%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
-- LOGIC BEGINS HERE
--%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

BEGIN

-- SLINK LOOPBACK (moved to stim file, 3-8-04)

--SL0_RX <= SL0_TX;
--SL1_RX <= SL1_TX;
--SL2_RX <= SL2_TX;
--SL3_RX <= SL3_TX;
--SL4_RX <= SL4_TX;
--SL5_RX <= SL5_TX;
--SL6_RX <= SL6_TX;
-- must comment out IR_RX assignment to run initial interrupt test
--IR_RX <= IR_TX;



-- PORT MAPS
--******************************************************************************************		
TB_APP_RBX_CC_top: APP_RBX_CC_top PORT MAP(
XS_SDCLK0 => XS_SDCLK0,
--3-8-04 CLK_3_6MHz		=>  CLK_3_6MHz	,
XS_MD	=> XS_MD,
XS_MA => XS_MA,
XS_CS1n => XS_CS1n,
XS_CS2n => XS_CS2n,
XS_CS3n => XS_CS3n,
XS_CS4n => XS_CS4n,
XS_CS5n=> XS_CS5n,
XS_nOE=> XS_nOE,
XS_nPWE => XS_nPWE,
XS_RDnWR => XS_RDnWR,
XS_DQM => XS_DQM,
XS_RDY => XS_RDY,
XS_SDCKE0		=>  XS_SDCKE0	,
XS_DREQ0		=>XS_DREQ0	,
XS_DREQ1		=> XS_DREQ1	,
XS_PWM1 => XS_PWM1,
XS_PWM0 => XS_PWM0,
XS_nPOE		=>	XS_nPOE,	
XS_nPIOW		=>	XS_nPIOW,	
XS_nPIOR		=>	XS_nPIOR,	
XS_nPCE2		=>XS_nPCE2	,
XS_nPCE1		=>	XS_nPCE1	,
XS_nIOIS16		=>	XS_nIOIS16	,
XS_nPWAIT		=>	XS_nPWAIT	,
XS_nPSKTSEL		=>	XS_nPSKTSEL	,
XS_nPREG 		=>	XS_nPREG 	,
XS_BITCLK 		=>	XS_BITCLK 	,
XS_SDATA_IN0 	=> XS_SDATA_IN0 ,
XS_SDATA_IN1 	=> XS_SDATA_IN1 ,
XS_SDATA_OUT 	=> XS_SDATA_OUT ,
XS_SYNC 		=>	XS_SYNC 	,
XS_nACRESET 	=>	XS_nACRESET ,
SL6_TX => SL6_TX,
SL5_TX => SL5_TX,
SL4_TX => SL4_TX,
IR_TX => IR_TX,
FF_RTS => FF_RTS,
LED_4 => LED_4,
SL6_RX => SL6_RX,
SL5_RX => SL5_RX,
SL4_RX => SL4_RX,
IR_RX => IR_RX,
FF_CTS => FF_CTS,
I2S_MCLK_GCK3 => I2S_MCLK_GCK3,
I2S_SCLK_GCK1 => I2S_SCLK_GCK1,
ETH_A => ETH_A,
ETH_D => ETH_D,
ETH_BE_N => ETH_BE_N,
ETH_CS => ETH_CS,
ETH_ADS_N => ETH_ADS_N,
ETH_RD_N => ETH_RD_N,
ETH_WR_N => ETH_WR_N,
ETH_RESET => ETH_RESET,
ETH_INT => ETH_INT,
ETH_CYCLE_N => ETH_CYCLE_N,
ETH_DATACS_N => ETH_DATACS_N,
ETH_RDYRTN_N => ETH_RDYRTN_N,
ETH_W_R_N => ETH_W_R_N,
ETH_LCLK => ETH_LCLK,
ETH_VLBUS_N => ETH_VLBUS_N,
ETH_LDEV_N => ETH_LDEV_N,
ETH_IOWAIT => ETH_IOWAIT,
I2S_MCLK => I2S_MCLK,
I2S_SCLK => I2S_SCLK,
I2S_LRCLK => I2S_LRCLK,
I2S_SDATA => I2S_SDATA,
DA_INT_8405A => DA_INT_8405A,
DA_INT_8415A => DA_INT_8415A,
DAI_ENABLE => DAI_ENABLE,
SL0_TX => SL0_TX,
SL1_TX => SL1_TX,
SL2_TX => SL2_TX,
SL3_TX => SL3_TX,
SL0_RX => SL0_RX,
SL1_RX => SL1_RX,
SL2_RX => SL2_RX,
SL3_RX => SL3_RX,
SW_1 => SW_1,
LED_0 => LED_0,
LED_1 => LED_1,
LED_2 => LED_2,
LED_3 => LED_3,
FPGA_EB90 => FPGA_EB90,
FPGA_EB92 => FPGA_EB92,
FPGA_EB93 => FPGA_EB93,
FPGA_EB94 => FPGA_EB94,
FPGA_EB96 => FPGA_EB96,
FPGA_EB98 => FPGA_EB98
  );


--******************************************************************************************		
--******************************************************************************************		
TB_APP_RBX_CC_STIM: APP_RBX_CC_STIM PORT MAP(

XS_MD => XS_MD,
XS_MA => XS_MA,
XS_CS1n => XS_CS1n,
XS_CS2n => XS_CS2n,
XS_CS3n => XS_CS3n,
XS_CS4n => XS_CS4n,
XS_CS5n => XS_CS5n,
XS_nOE => XS_nOE,
XS_nPWE => XS_nPWE,
XS_RDnWR => XS_RDnWR,
XS_DQM => XS_DQM,
XS_RDY => XS_RDY,
XS_SDCLK0 => XS_SDCLK0,
XS_PWM1 => XS_PWM1,
XS_PWM0 => XS_PWM0,
XS_SDCKE0 => XS_SDCKE0,
CLK_3_6MHz => CLK_3_6MHz,
XS_nIOIS16 => XS_nIOIS16,
XS_nPWAIT => XS_nPWAIT,
XS_nPREG => XS_nPREG,
XS_nPSKTSEL => XS_nPSKTSEL,
XS_nPCE2 => XS_nPCE2,
XS_nPCE1 => XS_nPCE1,
XS_nPIOW => XS_nPIOW,
XS_nPIOR => XS_nPIOR,
XS_nPOE => XS_nPOE,
XS_nACRESET => XS_nACRESET,
XS_DREQ0 => XS_DREQ0,
XS_DREQ1 => XS_DREQ1,
XS_BITCLK => XS_BITCLK,
XS_SDATA_IN0 => XS_SDATA_IN0,
XS_SDATA_IN1 => XS_SDATA_IN1,
XS_SDATA_OUT => XS_SDATA_OUT,
XS_SYNC => XS_SYNC,
SL6_TX => SL6_TX,
SL5_TX => SL5_TX,
SL4_TX => SL4_TX,
IR_TX => IR_TX,
FF_RTS => FF_RTS,
LED_4 => LED_4,
SL6_RX => SL6_RX,
SL5_RX => SL5_RX,
SL4_RX => SL4_RX,
IR_RX => IR_RX,
FF_CTS => FF_CTS,
I2S_MCLK_GCK3 => I2S_MCLK_GCK3,
I2S_SCLK_GCK1 => I2S_SCLK_GCK1,
ETH_INT => ETH_INT,
ETH_CYCLE_N =>ETH_CYCLE_N,
ETH_DATACS_N => ETH_DATACS_N,
ETH_RDYRTN_N => ETH_RDYRTN_N,
ETH_W_R_N => ETH_W_R_N,
ETH_LCLK => ETH_LCLK,
ETH_VLBUS_N => ETH_VLBUS_N,
ETH_LDEV_N => ETH_LDEV_N,
ETH_IOWAIT => ETH_IOWAIT,
I2S_MCLK => I2S_MCLK,
I2S_SCLK => I2S_SCLK,
I2S_LRCLK => I2S_LRCLK,
I2S_SDATA => I2S_SDATA,
DA_INT_8405A => DA_INT_8405A,
DA_INT_8415A => DA_INT_8415A,
DAI_ENABLE => DAI_ENABLE,
SL3_TX => SL3_TX,
SL2_TX => SL2_TX,
SL1_TX => SL1_TX,
SL0_TX => SL0_TX,
SL3_RX => SL3_RX,
SL2_RX => SL2_RX,
SL1_RX => SL1_RX,
SL0_RX => SL0_RX,
SW_1 => SW_1,
LED_0 => LED_0,
LED_1 => LED_1,
LED_2 => LED_2,
LED_3 => LED_3,
FPGA_EB90 => FPGA_EB90,
FPGA_EB92 => FPGA_EB92,
FPGA_EB93 => FPGA_EB93,
FPGA_EB94 => FPGA_EB94,
FPGA_EB96 => FPGA_EB96,
FPGA_EB98 => FPGA_EB98	);

--******************************************************************************************	
TB_ETH_TARGET : ETH_TARGET  PORT MAP(
	SIM_CLK => XS_SDCLK0,
	DATA => ETH_D, 
	ADDR => ETH_A,
	E0_CS => ETH_CS,
	E0_ADS => ETH_ADS_N,
	E0_RD => ETH_RD_N,
	E0_WR => ETH_WR_N,
	E1_RESET => ETH_RESET);	


--******************************************************************************************		


--******************************************************************************************
-- STATIC PULLUPS and/or PULLDOWNS
--******************************************************************************************
-- pull these signals statically high/low for simulation
--Pullup RDY
XS_RDY <= 'H';

--******************************************************************************************
--  signals to spy on (place after "begin"):
spy_process : process
  begin
--init_signal_spy(DISP_currentState_path, "top_DISP_currentState",1);
init_signal_spy(FAIL_FLAG_path, "top_FAIL_FLAG",1);


wait;
  end process spy_process;
--end of signals to spy on:
--******************************************************************************************

END functional_tst;


Configuration Simulate_this of APP_RBX_CC_TB is
	for functional_tst
	end for;
end Simulate_this;

