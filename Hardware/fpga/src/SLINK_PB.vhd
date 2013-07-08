----------------------------------------------------------------------------
--
--  File:   SLINK_PB.vhd
--  Rev:    1.0.0
--  Date:	2-27-04
--
-- This is the VHDL module for the SLINK Register Interface for the SLINK Peripheral Module
-- in the Streetfire RBX Companion Chip (FPGA) for Streetfire Street Racer CPU Card to 
--  Application Board Interface
--
--	History:
--
--    New Register Set and FIFO Removal, 2-27-04
--	Added RX FIFO Overflow Support, 2-24-04 
--	Added TX Collision Support, 2-17-04
--	Added TX/RX FIFO Versions, and read pulse, 2-16-04
--	Created 2-6-04
----------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;

library unisim;
use unisim.ALL;

entity SLINK_PB is 
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
end SLINK_PB;


architecture rtl of SLINK_PB is


-- SIGNAL DEFINITIONS:

signal SCLK: std_logic;

-- Tx specific
signal SL_XDV	:std_logic_vector(7 downto 0); -- SLINK Transmit data valid (pulse from register write)
signal	ENA_TX	:std_logic_vector(7 downto 0); -- SLINK Enable from control register

signal	INT_TS	:std_logic_vector(7 downto 0);  -- Transmit Service Requested interrupt output(active high)
signal  INT_TC  :std_logic_vector(7 downto 0); -- TX collision,  2-20-04
signal	SEND_SL0,
	SEND_SL1,
	SEND_SL2,
	SEND_SL3,
	SEND_SL4,
	SEND_SL5,
	SEND_SL6,
	SEND_SL7:std_logic_vector(7 downto 0); -- SLINK SEND REGISTERS

-- Rx Specific
signal	ENA_RX  :std_logic_vector(7 downto 0); -- SLINK Enable from control register

signal	INT_RS	:std_logic_vector(7 downto 0);  -- Receive Service Requested interrupt output(active high)
signal	RCV_SL0,
	RCV_SL1,
	RCV_SL2,
	RCV_SL3,
	RCV_SL4,
	RCV_SL5,
	RCV_SL6,
	RCV_SL7	 :std_logic_vector(7 downto 0); -- Receive  Count (low time) 

-- Interrupt Specific

signal	CLR_TS	:std_logic_vector(7 downto 0); -- Interrupt Clear Inputfor bits (31:24)
signal	CLR_RS	:std_logic_vector(7 downto 0); -- Interrupt Clear Inputfor bits (23:16)
signal	CLR_TC	:std_logic_vector(7 downto 0); -- Interrupt Clear Inputfor bits (7:0)
signal	MASK_TS	:std_logic_vector(7 downto 0); -- Interrupt Mask Inputfor bits (31:24)
signal	MASK_RS	:std_logic_vector(7 downto 0); -- Interrupt Mask Inputfor bits (23:16)
signal	MASK_TC	:std_logic_vector(7 downto 0); -- Interrupt Mask Inputfor bits (7:0)
signal	STAT_TS	:std_logic_vector(7 downto 0); -- Interrupt Status output for bits (31:24)
signal	STAT_RS	:std_logic_vector(7 downto 0); -- Interrupt Status output for bits (23:16)
signal	STAT_RO	:std_logic_vector(7 downto 0); -- Interrupt Status output for bits (15:8)
signal	STAT_TC	:std_logic_vector(7 downto 0); -- Interrupt Status output for bits (7:0)
signal	SL_INT	:std_logic;  -- oring of 32 individual SL interrupts

signal READ_PULSE: std_logic_vector(7 downto 0);
signal CLR_TX_FIFO: std_logic_vector(7 downto 0); 
signal CLR_RX_FIFO: std_logic_vector(7 downto 0); 

signal RXFIFO_CT0,
	RXFIFO_CT1,
	RXFIFO_CT2,
	RXFIFO_CT3,
	RXFIFO_CT4,
	RXFIFO_CT5,
	RXFIFO_CT6,
	RXFIFO_CT7 :std_logic_vector(7 downto 0);
signal TXFIFO_CT0,
	TXFIFO_CT1,
	TXFIFO_CT2,
	TXFIFO_CT3,
	TXFIFO_CT4,
	TXFIFO_CT5,
	TXFIFO_CT6,
	TXFIFO_CT7 :std_logic_vector(7 downto 0);

signal INT_LI,CLR_LI,MASK_LI,STAT_LI  :std_logic_vector(7 downto 0); -- Interrupt /Status output for Line idle 
signal STAT_TF:std_logic_vector(7 downto 0); -- Interrupt /Status output for TX FIFO FULL
signal STAT_RE:std_logic_vector(7 downto 0); -- Interrupt /Status output for TX FIFO FULL


-- Component DEFINITIONS:
--------------------------------------------------------------
component SCLK_GEN 
	port ( 
	RST_I	:in std_logic; -- master reset for peripheral bus
	CLK_I	:in std_logic; -- master clock for peripheral bus (100MHz)
	SCLK	:out std_logic -- SLINK CLock (timebase of 10us)
	);
end component; --SCLK_GEN
--------------------------------------------------------------

--------------------------------------------------------------
component SL_Channels 
	port ( 
	RST_I	:in std_logic; -- master reset for peripheral bus
	CLK_I	:in std_logic; -- master clock for peripheral bus
	PBEN_I  :in std_logic;
	SCLK	:in std_logic; -- SLINK CLock (timebase of 10us)
	SL_XDV	:in std_logic_vector(7 downto 0); -- SLINK Transmit data valid (pulse from register write)
	ENA_TX	:in std_logic_vector(7 downto 0); -- SLINK Enable from control register
	SL_TX	:out std_logic_vector(7 downto 0); -- SLINK TX signal
	INT_TS	:out std_logic_vector(7 downto 0);  -- Transmit Service Requested interrupt output(active high)
	INT_TC	:out std_logic_Vector(7 downto 0);  -- TX collision interrupt
	STAT_TF	:out std_logic_Vector(7 downto 0);  -- TX FIFO FULL STATUS
	SEND_SL0,
	SEND_SL1,
	SEND_SL2,
	SEND_SL3,
	SEND_SL4,
	SEND_SL5,
	SEND_SL6,
	SEND_SL7:in std_logic_vector(7 downto 0); -- Transmit Send Count (low time) from reg.
	TXFIFO_CT0,
	TXFIFO_CT1,
	TXFIFO_CT2,
	TXFIFO_CT3,
	TXFIFO_CT4,
	TXFIFO_CT5,
	TXFIFO_CT6,
	TXFIFO_CT7 :out std_logic_vector(7 downto 0);
	CLR_TX_FIFO:in std_logic_vector(7 downto 0);  -- Active high synchronous FIFO Clear
	ENA_RX	:in  std_logic_vector(7 downto 0); -- SLINK Enable from control register	
	SL_RX : in std_logic_vector(7 downto 0);  -- RX input
	STAT_RO	:out std_logic_vector(7 downto 0); -- Receive FIFO overflow Interrupt output (overflow = 1)
	STAT_RE	:out std_logic_vector(7 downto 0); -- Receive FIFO EMPTY STATUS
	INT_RS	:out std_logic_vector(7 downto 0);  -- Receive Service Requested interrupt output(active high)
	RCV_SL0,
	RCV_SL1,
	RCV_SL2,
	RCV_SL3,
	RCV_SL4,
	RCV_SL5,
	RCV_SL6,
	RCV_SL7	 :out std_logic_vector(7 downto 0); -- Receive  Count (low time) 
	READ_PULSE :in std_logic_vector(7 downto 0);
	RXFIFO_CT0,
	RXFIFO_CT1,
	RXFIFO_CT2,
	RXFIFO_CT3,
	RXFIFO_CT4,
	RXFIFO_CT5,
	RXFIFO_CT6,
	RXFIFO_CT7 :out std_logic_vector(7 downto 0);
	CLR_RX_FIFO:in std_logic_vector(7 downto 0);
	INT_LI :out std_logic_vector(7 downto 0)
);
end component; --SL_Channels
--------------------------------------------------------------
--------------------------------------------------------------
component SL_INT_CTRL 
	port ( 
	RST_I	:in std_logic; -- master reset for peripheral bus
	CLK_I	:in std_logic; -- master clock for peripheral bus
	PBEN_I	:in std_logic; -- active high peripheral block enable
	INT_TS	:in std_logic_vector(7 downto 0); -- Interrupt Input for bits (31:24)
	INT_RS	:in std_logic_vector(7 downto 0); -- Interrupt Input for bits (23:16)
	INT_TC	:in std_logic_vector(7 downto 0); -- Interrupt Input for bits (15:8)
	INT_LI	:in std_logic_vector(7 downto 0); -- Interrupt Input for bits (7:0)
	CLR_TS	:in std_logic_vector(7 downto 0); -- Interrupt Clear Inputfor bits (31:24)
	CLR_RS	:in std_logic_vector(7 downto 0); -- Interrupt Clear Inputfor bits (23:16)
	CLR_TC	:in std_logic_vector(7 downto 0); -- Interrupt Clear Inputfor bits (15:8)
	CLR_LI	:in std_logic_vector(7 downto 0); -- Interrupt Clear Inputfor bits (7:0)
	MASK_TS	:in std_logic_vector(7 downto 0); -- Interrupt Mask Inputfor bits (31:24)
	MASK_RS	:in std_logic_vector(7 downto 0); -- Interrupt Mask Inputfor bits (23:16)
	MASK_TC	:in std_logic_vector(7 downto 0); -- Interrupt Mask Inputfor bits (15:8)
	MASK_LI	:in std_logic_vector(7 downto 0); -- Interrupt Mask Inputfor bits (7:0)
	STAT_TS	:out std_logic_vector(7 downto 0); -- Interrupt Status output for bits (31:24)
	STAT_RS	:out std_logic_vector(7 downto 0); -- Interrupt Status output for bits (23:16)
	STAT_TC	:out std_logic_vector(7 downto 0); -- Interrupt Status output for bits (15:8)
	STAT_LI	:out std_logic_vector(7 downto 0); -- Interrupt Status output for bits (7:0)
	SL_INT  :out std_logic 			   -- Interrupt Output for SL Peripheral Block
	);
end component; --SL_INT_CTRL 
--------------------------------------------------------------
--------------------------------------------------------------
component SL_Registers 
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
	SL_INT		:in std_logic;  -- 'or' of 32 interrupt sources
	SL_XDV		:out std_logic_vector(7 downto 0); -- SLINK Transmit data valid (pulse from register write)
	
-- SLINK REGISTER (I/O to logic)

	STAT_RO,	-- Status Register B (04) Bits (31:24)
	STAT_TF,	-- Status Register B (04) Bits (23:16)
	STAT_RE,	-- Status Register B (04) Bits (15:8)
	STAT_PI,	-- Status Register B (04) Bits (7:0)
	STAT_TS,	-- Status Register A (00) Bits (31:24)
	STAT_RS,	-- Status Register A (00) Bits (23:16)
	STAT_TC,	-- Status Register A (00) Bits (15:8)
	STAT_LI	-- Status Register A (00) Bits (7:0)
			:in std_logic_vector(7 downto 0); -- from Interrupt latches

	MASK_TS,	-- Mask Register A (08) Bits (31:24)
	MASK_RS,	-- Mask Register A (08) Bits (23:16)
	MASK_TC,	-- Mask Register A (08) Bits (15:8)
	MASK_LI	-- Mask Register A (08) Bits (7:0)
			:out std_logic_vector(7 downto 0); -- to Interrupt Logic

	CLR_TS,	-- Clear Register A (10) Bits (31:24)
	CLR_RS,	-- Clear Register A (10) Bits (23:16)
	CLR_TC,	-- Clear Register A (10) Bits (15:8)
	CLR_LI	-- Clear Register A (10) Bits (7:0)	
			:out std_logic_vector(7 downto 0); -- to interrupt logic
	CLR_TX_FIFO,	-- Control Register A (18) Bits (31:24)
	CLR_RX_FIFO,	-- Control Register A (18) Bits (23:16)
	ENA_TX,		-- Control Register A (18) Bits (15:8)
	ENA_RX		-- Control Register A (18) Bits (7:0)	
			:out std_logic_vector(7 downto 0); --  to TX/RX SLINK Logic
	SEND_SL0,	-- SL Tx Data Register (20) for SL TX Channel 0
	SEND_SL1,	-- SL Tx Data Register (24) for SL TX Channel 1
	SEND_SL2,	-- SL Tx Data Register (28) for SL TX Channel 2
	SEND_SL3,	-- SL Tx Data Register (2C) for SL TX Channel 3
	SEND_SL4,	-- SL Tx Data Register (30) for SL TX Channel 4
	SEND_SL5,	-- SL Tx Data Register (34) for SL TX Channel 5
	SEND_SL6,	-- SL Tx Data Register (38) for SL TX Channel 6
	SEND_SL7	-- SL Tx Data Register (3C) for SL TX Channel 7
			:out std_logic_vector(7 downto 0); -- Tx Data Registers (output to TX logic)	
	RCV_SL0,	-- SL RX Data Register (40) for SL RX Channel 0	
	RCV_SL1,	-- SL RX Data Register (44) for SL RX Channel 1
	RCV_SL2,	-- SL RX Data Register (48) for SL RX Channel 2
	RCV_SL3,	-- SL RX Data Register (4C) for SL RX Channel 3
	RCV_SL4,	-- SL RX Data Register (50) for SL RX Channel 4
	RCV_SL5,	-- SL RX Data Register (54) for SL RX Channel 5
	RCV_SL6,	-- SL RX Data Register (58) for SL RX Channel 6
	RCV_SL7		-- SL RX Data Register (5C) for SL RX Channel 7
			:in std_logic_vector(7 downto 0); -- Rx Data Registers (input from RX logic)
	READ_PULSE	:out std_logic_vector(7 downto 0); -- active high read pulse of one clock on read
	TXFIFO_CT0,
	TXFIFO_CT1,
	TXFIFO_CT2,
	TXFIFO_CT3,
	TXFIFO_CT4,
	TXFIFO_CT5,
	TXFIFO_CT6,
	TXFIFO_CT7, 
	RXFIFO_CT0,
	RXFIFO_CT1,
	RXFIFO_CT2,
	RXFIFO_CT3,
	RXFIFO_CT4,
	RXFIFO_CT5,
	RXFIFO_CT6,
	RXFIFO_CT7 	:in std_logic_vector(7 downto 0)
);
end component; --SL_Registers
--------------------------------------------------------------


--**********************************************************
--  LOGIC BEGINS HERE:
--**********************************************************
  
begin

-- Port Maps:
--------------------------------------------------------------
INST_SCLK_GEN: SCLK_GEN
PORT MAP( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
SCLK 	=> SCLK
);
------------------------------------------------------------
--------------------------------------------------------------
INST_SL_Channels: SL_Channels
PORT MAP( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
PBEN_I  => PBEN_I,
SCLK 	=> SCLK,
SL_XDV  => SL_XDV,
ENA_TX  => ENA_TX,
SL_TX   => SL_TX,
INT_TS  => INT_TS,
INT_TC  => INT_TC,
STAT_TF => STAT_TF,
SEND_SL0 =>SEND_SL0,
SEND_SL1 =>SEND_SL1,
SEND_SL2 =>SEND_SL2,
SEND_SL3 =>SEND_SL3,
SEND_SL4 =>SEND_SL4,
SEND_SL5 =>SEND_SL5,
SEND_SL6 =>SEND_SL6,
SEND_SL7 =>SEND_SL7,
TXFIFO_CT0 => TXFIFO_CT0,
TXFIFO_CT1 => TXFIFO_CT1,
TXFIFO_CT2 => TXFIFO_CT2,
TXFIFO_CT3 => TXFIFO_CT3,
TXFIFO_CT4 => TXFIFO_CT4,
TXFIFO_CT5 => TXFIFO_CT5,
TXFIFO_CT6 => TXFIFO_CT6,
TXFIFO_CT7 => TXFIFO_CT7,
CLR_TX_FIFO => CLR_TX_FIFO,
ENA_RX  => ENA_RX,
SL_RX  	=> SL_RX,
STAT_RO	=> STAT_RO,
STAT_RE	=> STAT_RE,
INT_RS  => INT_RS,
RCV_SL0 => RCV_SL0,
RCV_SL1 => RCV_SL1,
RCV_SL2 => RCV_SL2,
RCV_SL3 => RCV_SL3,
RCV_SL4 => RCV_SL4,
RCV_SL5 => RCV_SL5,
RCV_SL6 => RCV_SL6,
RCV_SL7 => RCV_SL7,
READ_PULSE  =>    READ_PULSE,
RXFIFO_CT0 => RXFIFO_CT0,
RXFIFO_CT1 => RXFIFO_CT1,
RXFIFO_CT2 => RXFIFO_CT2,
RXFIFO_CT3 => RXFIFO_CT3,
RXFIFO_CT4 => RXFIFO_CT4,
RXFIFO_CT5 => RXFIFO_CT5,
RXFIFO_CT6 => RXFIFO_CT6,
RXFIFO_CT7 => RXFIFO_CT7,
CLR_RX_FIFO => CLR_RX_FIFO,
INT_LI => INT_LI

);
------------------------------------------------------------
--------------------------------------------------------------
INST_SL_INT_CTRL : SL_INT_CTRL 
PORT MAP(
RST_I	=> RST_I,
CLK_I	=> CLK_I,
PBEN_I  => PBEN_I,
INT_TS  => INT_TS,
INT_RS  => INT_RS,
INT_TC  => INT_TC,
INT_LI  => INT_LI,
CLR_TS  => CLR_TS,
CLR_RS  => CLR_RS,
CLR_TC  => CLR_TC,
CLR_LI  => CLR_LI,
MASK_TS  => MASK_TS,
MASK_RS  => MASK_RS,
MASK_TC  => MASK_TC,
MASK_LI  => MASK_LI,
STAT_TS  => STAT_TS,
STAT_RS  => STAT_RS,
STAT_TC  => STAT_TC,
STAT_LI  => STAT_LI,
SL_INT	=> SL_INT
);
------------------------------------------------------------
--------------------------------------------------------------
INST_SL_Registers: SL_Registers
PORT MAP(
RST_I	=>	RST_I	,
CLK_I	=>	CLK_I	,
WE_I	=>	WE_I	,
CYC_I	=>	CYC_I	,
ADR_I	=>	ADR_I	,
SEL_I	=>	SEL_I	,
DAT_I	=>	DAT_I	,
STB_I	=>	STB_I	,
DAT_O	=>	DAT_O	,
ACK_O	=>	ACK_O	,
PBEN_I	=>	PBEN_I	,
PBINT_O	=>	PBINT_O	,
SL_INT	=>	SL_INT	,
SL_XDV	=>	SL_XDV	,
STAT_RO 	=>	STAT_RO ,
STAT_TF 	=> 	STAT_TF,
STAT_RE	=>	STAT_RE,
STAT_PI 	=>	INT_LI,
STAT_TS	=>	STAT_TS	,
STAT_RS	=>	STAT_RS	,
STAT_TC 	=>	STAT_TC ,
STAT_LI	=>	STAT_LI	,
MASK_TS	=>	MASK_TS	,
MASK_RS	=>	MASK_RS	,
MASK_TC 	=>	MASK_TC ,
MASK_LI	=>	MASK_LI	,
CLR_TS	=>	CLR_TS	,
CLR_RS	=>	CLR_RS	,
CLR_TC  	=>	CLR_TC  ,
CLR_LI	=>	CLR_LI	,
CLR_TX_FIFO => 	CLR_TX_FIFO ,
CLR_RX_FIFO =>  CLR_RX_FIFO ,
ENA_TX	=>	ENA_TX	,
ENA_RX	=>	ENA_RX	,
SEND_SL0	=>	SEND_SL0	,
SEND_SL1	=>	SEND_SL1	,
SEND_SL2	=>	SEND_SL2	,
SEND_SL3	=>	SEND_SL3	,
SEND_SL4	=>	SEND_SL4	,
SEND_SL5	=>	SEND_SL5	,
SEND_SL6	=>	SEND_SL6	,
SEND_SL7	=>	SEND_SL7	,
RCV_SL0	=>	RCV_SL0	,
RCV_SL1	=>	RCV_SL1	,
RCV_SL2	=>	RCV_SL2	,
RCV_SL3	=>	RCV_SL3	,
RCV_SL4	=>	RCV_SL4	,
RCV_SL5	=>	RCV_SL5	,
RCV_SL6	=>	RCV_SL6	,
RCV_SL7	=>	RCV_SL7	,
READ_PULSE  =>    READ_PULSE,
TXFIFO_CT0  =>	TXFIFO_CT0,
TXFIFO_CT1  =>	TXFIFO_CT1,
TXFIFO_CT2  =>	TXFIFO_CT2,
TXFIFO_CT3  =>	TXFIFO_CT3,
TXFIFO_CT4  =>	TXFIFO_CT4,
TXFIFO_CT5  =>	TXFIFO_CT5,
TXFIFO_CT6  =>	TXFIFO_CT6,
TXFIFO_CT7  =>	TXFIFO_CT7, 
RXFIFO_CT0  =>	RXFIFO_CT0,
RXFIFO_CT1  =>	RXFIFO_CT1,
RXFIFO_CT2  =>	RXFIFO_CT2,
RXFIFO_CT3  =>	RXFIFO_CT3,
RXFIFO_CT4  =>	RXFIFO_CT4,
RXFIFO_CT5  =>	RXFIFO_CT5,
RXFIFO_CT6  =>	RXFIFO_CT6,
RXFIFO_CT7  =>	RXFIFO_CT7 
);
------------------------------------------------------------





--**********************************************************

end rtl;

