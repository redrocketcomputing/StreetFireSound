--------------------------------------------------------
-- FILE: TEST_Companion_TB.vhd 
--
-- Date: 3-6-04
-- Author: Robyn Bauer
-- Purpose: Streetfire StreetRacer CPU Card to RBX Companion Chip (FPGA) Verification--
-- Note: This module is NOT syntheziable! It if for simulation only !!!
--
-- Revision: 0.0
--
-- History:  
--	
--	Removed CLK_3_6MHz from top level design, 3-6-04
--	Moved SLINK loopback to STIM file, 2-23-04
--	Added SLINK Loopback SL_RX(k) <= SL_TX(k), k=0,1,...,6 and IR_RX <= IR_TX, 2-10-04, REB
--	(Need to break IR_RX/IR_TX loop to run previous interrupt verification test)
--		
--	-> initial draft 11-25-03
-- 
--
-- -------------------------------------------------------

LIBRARY ieee;
USE ieee.std_logic_1164.ALL;
USE ieee.numeric_std.ALL;

library modelsim_lib;   
use modelsim_lib.util.all;

ENTITY TEST_Companion_TB is				
END TEST_Companion_TB;

ARCHITECTURE functional_tst of TEST_Companion_TB is


--Signals
signal top_FAIL_FLAG:std_logic;
constant FAIL_FLAG_path: string:= "/TEST_Companion_tb/tb_TEST_Companion_stim/fail_flag";


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
signal XS_SDCLK0:std_logic;	-- Synchronous Static Memory clock from CPU
signal XS_PWM1:std_logic;	--XS GPIO17 -use as fpga reset input (or PWM1 from CPU)
signal XS_PWM0:std_logic;	--XS GPIO16 -use as interrupt output (or PWM0 from cpu)
signal 	SL0_TX:  std_logic; 
signal 	SL1_TX:  std_logic; 
signal 	SL2_TX: std_logic; 
signal 	SL3_TX:  std_logic;
signal 	SL4_TX: std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_SSP_TXD/GPIO[25]
signal 	SL5_TX: std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_FF_RI/GPIO[38]
signal 	SL6_TX: std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_FF_DCD/GPIO[36]
signal 	IR_TX: std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_SSP_RXD/GPIO[26]
signal 	SL0_RX:  std_logic; 
signal 	SL1_RX:  std_logic; 
signal 	SL2_RX:  std_logic; 
signal 	SL3_RX:  std_logic;
signal 	SL4_RX: std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_SSPEXTCLK/GPIO[27]
signal 	SL5_RX: std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_FF_DSR/GPIO[37]
signal 	SL6_RX: std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_FF_DTR/GPIO[40]
signal 	IR_RX:  std_logic; --Triple Connection: CPU/FPGA/EDGE:XS_SSP_SFRM/GPIO[24]

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
signal 	EB_GCK3_13	:std_logic; --LOC="C8" GCK3 from edge NOT CONNECTED on test board
signal 	EB_GCK1_147	:std_logic;	--LOC="T8" GCK1 from edge (test board I2S_SCLK)
signal 	ADDR		:std_logic_vector(15 downto 1); -- Test Board Address Bus
signal 	DATA		:std_logic_vector(31 downto 0); -- Test Board Data Bus
signal 	E0_CS:  std_logic; 
signal 	E0_ADS:  std_logic; 
signal 	E0_RD:  std_logic; 
signal 	E0_WR:  std_logic; 
signal 	E1_CS:  std_logic; 
signal 	E1_IOR:  std_logic; 
signal 	E1_IOW:  std_logic;
signal 	E1_RESET:  std_logic; 
signal 	E0_CYCLE:  std_logic; 
signal 	E0_DATACS:  std_logic;
signal 	E0_RDYRTN:  std_logic;
signal 	E0_W_RN:  std_logic; 
signal 	E0_LCLK:  std_logic; 
signal 	E0_INT:  std_logic;
signal 	E0_LDEV:  std_logic;
signal 	E0_IOWAIT:  std_logic; 
signal 	E1_INT:  std_logic;
signal 	E1_IO16:  std_logic; 
signal 	E1_IO32:  std_logic;
signal 	E1_IOWAIT:  std_logic; 
signal 	I2S_SCLK:  std_logic;
signal 	I2S_SDATA:  std_logic; 
signal 	I2S_LRCLK:  std_logic;
signal 	I2S_MCLK:  std_logic;

-----------------------------------------------
--$CONFIG$			
 -- comment out for unmodified (non-byte enabled) test board
 signal ETH_BE : std_logic_vector (3 downto 0); 
---------------------------------------------


--******************************************************************************************
--signals to spy on:
--signal top_CFI_WAIT_N:std_logic;
--constant CFI_WAIT_N_path: string:= "/ls_tb/uut/digilab_cpld/cfi_wait_n";
-- end of signals to spy on:  See init_signal_spy procedure calls below:
--******************************************************************************************


-- COMPONENT DEFINITIONS:

--++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	COMPONENT TEST_Companion_top 
    Port (
-- XSCALE CLOCK INPUTS:
	XS_SDCLK0	:in	std_logic;	--LOC = "B8" ; #GCK2 Synchronous Static Memory clock from CPU
--3-6-04	CLK_3_6MHz	:in	std_logic;	--LOC = "T9" ; #GCK0 (x-scale 3.6MHz/GPIO[11])

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
	
	-- IO which is different between App/Test Boards:


----------------------------------------------------------------------------------------
--$CONFIG$
	-- APP BOARD/MODIFIED TEST BOARD:
----------------------------------------------------------------------------------------
	ETH_BE : out std_logic_vector(3 downto 0);
	-- FPGA PIN T3 IS ETH_BE<0>  Ethernet BE0 (R23) --> (R6) EB140 (FPGA-T3) (was E1_IOWAIT)
	-- FPGA PIN M2 IS ETH_BE<1>  Ethernet BE1 (R24) --> (R7) EB129 (FPGA-M2) (was E1_CS)
	-- FPGA PIN R4 IS ETH_BE<2>  Ethernet BE2 (R25) --> (R8) EB139 (FPGA-R4) (was E1_INT)
	-- FPGA PIN P1 IS ETH_BE<3>  Ethernet BE3 (R26) --> (R9) EB136 (FPGA-P1) (was E1_IO16)
----------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------
	-- UNMODIFIED TEST BOARD:
----------------------------------------------------------------------------------------
--	E1_IOWAIT: IN std_logic;	-- FPGA PIN T3, EB140 on unmodified test board
--	E1_CS: OUT std_logic; 			-- FPGA PIN M2, EB129 on unmodified test board
--	E1_INT: IN std_logic; 			-- FPGA PIN R4, EB139 on unmodified test board
--	E1_IO16: IN std_logic; 		-- FPGA PIN P1, EB136 on unmodified test board
------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------

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
END COMPONENT; -- TEST_Companion_top 
--******************************************************************************************

--******************************************************************************************
	COMPONENT TEST_Companion_STIM
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
--	XS_SDCKE0	:out std_logic;	-- SDRAM and/or Sync Static Memory clock enable (SDCKE0) from CPU
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

-- from FPGA Output Register:
	XS_DREQ0, 	
	XS_DREQ1, 	
	XS_BITCLK, 	
	XS_SDATA_IN0, 
	XS_SDATA_IN1, 
	XS_SDATA_OUT, 
	XS_SYNC, 	
	LED_4, 	
	FF_RTS, 	
	IR_TX, 	
	SL6_TX, 	
	SL5_TX, 	
	SL4_TX, 	
	SL3_TX, 	
	SL2_TX, 	
	SL1_TX, 	
	SL0_TX, 	
	I2S_LRCLK, 	
	I2S_MCLK, 	
	E0_CYCLE, 	
	E0_DATACS, 	
	E0_RDYRTN,	
	E0_W_RN, 	
	E0_LCLK, 	
	E1_RESET : in std_logic; 	

	I2S_SCLK,I2S_SDATA : inout std_logic;

EB_GCK3_13,
FF_CTS,
IR_RX,
SL6_RX,
SL5_RX,
SL4_RX,
SL3_RX,
SL2_RX,
SL1_RX,
SL0_RX,
EB_GCK1_147,
E1_IO16,
E1_IO32,
E1_IOWAIT,
E0_LDEV,
E0_IOWAIT,
E1_INT,
E0_INT : out std_logic	);	
END COMPONENT; -- TEST_Companion_STIM 


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

--SL0_RX <= SL0_TX;
--SL1_RX <= SL1_TX;
--SL2_RX <= SL2_TX;
--SL3_RX <= SL3_TX;
--SL4_RX <= SL4_TX;
--SL5_RX <= SL5_TX;
--SL6_RX <= SL6_TX;
--IR_RX <= IR_TX;


-- PORT MAPS
--******************************************************************************************		
TB_TEST_Companion_top: TEST_Companion_top PORT MAP(
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
XS_SDCLK0 => XS_SDCLK0,
XS_PWM1 => XS_PWM1,
XS_PWM0 => XS_PWM0,
XS_SDCKE0	=>  XS_SDCKE0	,
--3-6-04 CLK_3_6MHz	=>  CLK_3_6MHz	,
XS_DREQ0	=>XS_DREQ0	,
XS_DREQ1	=> XS_DREQ1	,
XS_nPOE		=>	XS_nPOE,	
XS_nPIOW	=>	XS_nPIOW,	
XS_nPIOR	=>	XS_nPIOR,	
XS_nPCE2	=>XS_nPCE2	,
XS_nPCE1	=>	XS_nPCE1	,
XS_nIOIS16	=>	XS_nIOIS16	,
XS_nPWAIT	=>	XS_nPWAIT	,
XS_nPSKTSEL	=>	XS_nPSKTSEL	,
XS_nPREG 	=>	XS_nPREG 	,
XS_BITCLK 	=>	XS_BITCLK 	,
XS_SDATA_IN0 	=> XS_SDATA_IN0 ,
XS_SDATA_IN1 	=> XS_SDATA_IN1 ,
XS_SDATA_OUT 	=> XS_SDATA_OUT ,
XS_SYNC 	=>	XS_SYNC 	,
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
EB_GCK3_13 => EB_GCK3_13,
EB_GCK1_147 => EB_GCK1_147,
ADDR => ADDR,
DATA => DATA,
E0_CS => E0_CS,
E0_ADS => E0_ADS,
E0_RD => E0_RD,
E0_WR => E0_WR,
E1_IOR => E1_IOR,
E1_IOW => E1_IOW,
E1_RESET => E1_RESET,
E0_CYCLE => E0_CYCLE,
E0_DATACS => E0_DATACS,
E0_RDYRTN => E0_RDYRTN,
E0_W_RN => E0_W_RN,
E0_LCLK => E0_LCLK,
E0_INT => E0_INT,
E0_LDEV => E0_LDEV,
E0_IOWAIT => E0_IOWAIT,
E1_IO32 => E1_IO32,

-------------------------
-- comment in only one of the following groups:
--$CONFIG$
----------------------------------------------------------------------------------------
-- for test boards with BYTE ENABLES from FPGA
ETH_BE => ETH_BE,
------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------
-- for test boards without BYTE ENABLES feedthrough from FPGA

--E1_IOWAIT => E1_IOWAIT, --Ethernet BE0 (R23) --> (R6) EB140 (FPGA-T3) (was E1_IOWAIT)
--E1_CS => E1_CS,	-- Ethernet BE1 (R24) --> (R7) EB129 (FPGA-M2) (was E1_CS)
--E1_INT => E1_INT, --Ethernet BE2 (R25) --> (R8) EB139 (FPGA-R4) (was E1_INT)
--E1_IO16 => E1_IO16, --Ethernet BE3 (R26) --> (R9) EB136 (FPGA-P1) (was E1_IO16)
------------------------------------------------------------------------------------------

I2S_SCLK => I2S_SCLK,
I2S_SDATA => I2S_SDATA,
I2S_LRCLK => I2S_LRCLK,
I2S_MCLK => I2S_MCLK,
SL0_TX => SL0_TX,
SL1_TX => SL1_TX,
SL2_TX => SL2_TX,
SL3_TX => SL3_TX,
SL0_RX => SL0_RX,
SL1_RX => SL1_RX,
SL2_RX => SL2_RX,
SL3_RX => SL3_RX  );


--******************************************************************************************		
--******************************************************************************************		
TB_TEST_Companion_STIM: TEST_Companion_STIM PORT MAP(

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
-- XS_SDCKE0 => XS_SDCKE0,
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
LED_4 => LED_4,
FF_RTS => FF_RTS,
IR_TX => IR_TX,
SL6_TX => SL6_TX,
SL5_TX => SL5_TX,
SL4_TX => SL4_TX,
SL3_TX => SL3_TX,
SL2_TX => SL2_TX,
SL1_TX => SL1_TX,
SL0_TX => SL0_TX,
I2S_LRCLK => I2S_LRCLK,
I2S_MCLK => I2S_MCLK,
I2S_SCLK => I2S_SCLK,
I2S_SDATA => I2S_SDATA,
E0_CYCLE => E0_CYCLE,
E0_DATACS => E0_DATACS,
E0_RDYRTN => E0_RDYRTN,
E0_W_RN => E0_W_RN,
E0_LCLK => E0_LCLK,
E1_RESET => E1_RESET,
EB_GCK3_13 => EB_GCK3_13,
FF_CTS => FF_CTS,
IR_RX => IR_RX,
SL6_RX => SL6_RX,
SL5_RX => SL5_RX,
SL4_RX => SL4_RX,
SL3_RX => SL3_RX,
SL2_RX => SL2_RX,
SL1_RX => SL1_RX,
SL0_RX => SL0_RX,
EB_GCK1_147 => EB_GCK1_147,
E1_IO16 => E1_IO16,
E1_IO32 => E1_IO32,
E1_IOWAIT => E1_IOWAIT,
E0_LDEV => E0_LDEV,
E0_IOWAIT => E0_IOWAIT,
E1_INT => E1_INT,
E0_INT => E0_INT
				);

--******************************************************************************************	
TB_ETH_TARGET : ETH_TARGET  PORT MAP(
	SIM_CLK => XS_SDCLK0,
	DATA => DATA, 
	ADDR => ADDR(3 downto 1),
	E0_CS => E0_CS,
	E0_ADS => E0_ADS,
	E0_RD => E0_RD,
	E0_WR => E0_WR,
	E1_RESET => E1_RESET);	


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


Configuration Simulate_this of TEST_Companion_TB is
	for functional_tst
	end for;
end Simulate_this;

