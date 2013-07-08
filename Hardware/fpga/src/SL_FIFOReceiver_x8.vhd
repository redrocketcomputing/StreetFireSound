----------------------------------------------------------------------------
--
--  File:   SL_FIFOReceiver_x8.vhd
--  Rev:    1.0.0
--  Date:	3-4-04
--  This is the Wrapper VHDL module for the 8 SLINK Receivers with FIFOs for the SLINK Peripheral Module
-- in the Streetfire RBX Companion Chip (FPGA) for Streetfire Street Racer CPU Card to 
--  Application Board Interface
--
--  Author: Robyn E. Bauer
--
--	History: 
--	
--	Created 3-4-04 (modified from SL_Receiver_x8.vhd)
----------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;

library unisim;
use unisim.ALL;

entity SL_Receiver_x8 is 
	generic(BV_MIN_ECOUNT,BV_RX_ALMOST_FULL_CT: bit_vector (7 downto 0)  ); 
	port ( 
	RST_I	:in std_logic; -- master reset for peripheral bus
	CLK_I	:in std_logic; -- master clock for peripheral bus
	PBEN_I  : in std_logic;
	SCLK	:in std_logic; -- SLINK CLock (timebase of 10us)
	ENA_RX :in std_logic_vector(7 downto 0); -- SLINK Enable from control register
	SL_RX	: in std_logic_vector(7 downto 0); -- SLINK RX signal
	STAT_RO	:out std_logic_vector(7 downto 0); -- Receive FIFO overflow Interrupt output (overflow = 1)
	STAT_RE	:out std_logic_vector(7 downto 0); -- Receive FIFO EMPTY STATUS
	INT_RS	:out std_logic_vector(7 downto 0);  -- Receive Service Requested interrupt output(active high)
	READ_PULSE:in std_logic_vector(7 downto 0);
	CLR_RX_FIFO:in std_logic_vector(7 downto 0);
	RCV_SL0,
	RCV_SL1,
	RCV_SL2,
	RCV_SL3,
	RCV_SL4,
	RCV_SL5,
	RCV_SL6,
	RCV_SL7	  :out std_logic_vector(7 downto 0); -- Receive  Count (low time) 
	RXFIFO_CT0,
	RXFIFO_CT1,
	RXFIFO_CT2,
	RXFIFO_CT3,
	RXFIFO_CT4,
	RXFIFO_CT5,
	RXFIFO_CT6,
	RXFIFO_CT7
			 :out std_logic_vector(7 downto 0)
);
end SL_Receiver_x8;


architecture rtl of SL_Receiver_x8 is

component SL_RX_Channel
	generic(BV_MIN_ECOUNT: bit_vector (7 downto 0)  ); 
	port ( 
	RST_I	:in std_logic; -- master reset for peripheral bus
	CLK_I	:in std_logic; -- master clock for peripheral bus
	PBEN_I  : in std_logic;
	SCLK	:in std_logic; -- SLINK CLock (timebase of 10us)
	ENA_RX :in std_logic; -- SLINK Enable from control register
	SL_RX	: in std_logic; -- SLINK RX signal
	SLRX_CT :out std_logic_vector(7 downto 0); -- Receive  Count (low time) 	
	SL_RDV	:out std_logic;
	RX_READ :in std_logic
);

end component; -- SL_RX_Channel


component SL_RX_IND_FIFOS_x8
	generic(BV_RX_ALMOST_FULL_CT: bit_vector (7 downto 0)  ); 
	port (
	RST_I	:in std_logic; -- master reset for peripheral bus
	CLK_I	:in std_logic; -- master clock for peripheral bus
	PBEN_I  :in std_logic;
	ENA_RX  :in std_logic_vector(7 downto 0);  -- SLINK Enable from control register
	SL_RDV :in std_logic_vector(7 downto 0); 
	CLR_RX_FIFO :in std_logic_vector(7 downto 0);  
	RX_READ : out std_logic_vector(7 downto 0);  
	SLRX_CT0,
	SLRX_CT1,
	SLRX_CT2,
	SLRX_CT3,
	SLRX_CT4,
	SLRX_CT5,
	SLRX_CT6,
	SLRX_CT7: in std_logic_vector(7 downto 0);
	READ_PULSE: in std_logic_vector(7 downto 0);
	RXFIFO_OVERFLOW: out std_logic_vector(7 downto 0);
	RXFIFO_RE_EMPTY: out std_logic_vector(7 downto 0);
	RXFIFO_ALMOST_FULL: out std_logic_vector(7 downto 0);
	RCV_SL0,
	RCV_SL1,
	RCV_SL2,
	RCV_SL3,
	RCV_SL4,
	RCV_SL5,
	RCV_SL6,
	RCV_SL7: out std_logic_vector(7 downto 0);
	RXFIFO_CT0,
	RXFIFO_CT1,
	RXFIFO_CT2,
	RXFIFO_CT3,
	RXFIFO_CT4,
	RXFIFO_CT5,
	RXFIFO_CT6,
	RXFIFO_CT7: out std_logic_vector(7 downto 0)
	);
end component; --SL_RX_IND_FIFOS_x8


-- SIGNAL DEFINITIONS:

signal SL_RDV:std_logic_vector(7 downto 0);
signal RX_READ:std_logic_vector(7 downto 0);
signal SLRX_CT0,
	SLRX_CT1,
	SLRX_CT2,
	SLRX_CT3,
	SLRX_CT4,
	SLRX_CT5,
	SLRX_CT6,
	SLRX_CT7: std_logic_vector(7 downto 0);
signal RXFIFO_OVERFLOW,
	RXFIFO_RE_EMPTY,
	RXFIFO_ALMOST_FULL: std_logic_vector(7 downto 0);

--**********************************************************
--  LOGIC BEGINS HERE:
--**********************************************************
  
begin

INT_RS 	<= RXFIFO_ALMOST_FULL; -- RCV Request Service flag (fifo version -assign to RXFIFO_ALMOST_FULL)

STAT_RO 	<= RXFIFO_OVERFLOW; -- RCV FIFO OVerflow flag
STAT_RE	<= RXFIFO_RE_EMPTY; -- RCV FIFO Empty flag

--non-fifo version code:
--INT_RS 	<= SL_RDV ; -- RCV Request Service flag (non fifo-assign to SL_RDV)
--STAT_RO 	<= (others => '0'); -- RCV FIFO OVerflow flag
--STAT_RE	<= (others => '0'); -- RCV FIFO Empty flag
--RXFIFO_CT0 <= (others => '0');
--RXFIFO_CT1 <= (others => '0');
--RXFIFO_CT2 <= (others => '0');
--RXFIFO_CT3 <= (others => '0');
--RXFIFO_CT4 <= (others => '0');
--RXFIFO_CT5 <= (others => '0');
--RXFIFO_CT6 <= (others => '0');
--RXFIFO_CT7 <= (others => '0');



-- Component Instantiations:
-------------------------------------------------
INST_SL_RX_IND_FIFOS_x8 : SL_RX_IND_FIFOS_x8
generic map (
BV_RX_ALMOST_FULL_CT => BV_RX_ALMOST_FULL_CT)
PORT MAP(
RST_I	=> RST_I,
CLK_I	=> CLK_I,
PBEN_I  => PBEN_I,
ENA_RX => ENA_RX ,
SL_RDV => SL_RDV ,
CLR_RX_FIFO => CLR_RX_FIFO ,
RX_READ => RX_READ ,
SLRX_CT0 => SLRX_CT0,
SLRX_CT1 => SLRX_CT1,
SLRX_CT2 => SLRX_CT2,
SLRX_CT3 => SLRX_CT3,
SLRX_CT4 => SLRX_CT4,
SLRX_CT5 => SLRX_CT5,
SLRX_CT6 => SLRX_CT6,
SLRX_CT7 => SLRX_CT7,
READ_PULSE => READ_PULSE,
RXFIFO_OVERFLOW => RXFIFO_OVERFLOW,
RXFIFO_RE_EMPTY => RXFIFO_RE_EMPTY,
RXFIFO_ALMOST_FULL => RXFIFO_ALMOST_FULL,
RCV_SL0 => RCV_SL0,
RCV_SL1 => RCV_SL1,
RCV_SL2 => RCV_SL2,
RCV_SL3 => RCV_SL3,
RCV_SL4 => RCV_SL4,
RCV_SL5 => RCV_SL5,
RCV_SL6 => RCV_SL6,
RCV_SL7 => RCV_SL7,
RXFIFO_CT0 => RXFIFO_CT0,
RXFIFO_CT1 => RXFIFO_CT1,
RXFIFO_CT2 => RXFIFO_CT2,
RXFIFO_CT3 => RXFIFO_CT3,
RXFIFO_CT4 => RXFIFO_CT4,
RXFIFO_CT5 => RXFIFO_CT5,
RXFIFO_CT6 => RXFIFO_CT6,
RXFIFO_CT7 => RXFIFO_CT7
);
-------------------------------------------------

-------------------------------------------------
INST0_SL_RX_Channel : SL_RX_Channel
generic map (
BV_MIN_ECOUNT => BV_MIN_ECOUNT )
PORT MAP( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
PBEN_I  => PBEN_I,
SCLK 	=> SCLK,
ENA_RX	=> ENA_RX(0),
SL_RX => SL_RX(0),
SLRX_CT => SLRX_CT0 ,
SL_RDV => SL_RDV(0),
RX_READ => RX_READ(0)
);

-------------------------------------------------
INST1_SL_RX_Channel : SL_RX_Channel
generic map (
BV_MIN_ECOUNT => BV_MIN_ECOUNT )
PORT MAP( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
PBEN_I  => PBEN_I,
SCLK 	=> SCLK,
ENA_RX	=> ENA_RX(1),
SL_RX => SL_RX(1),
SLRX_CT => SLRX_CT1 ,
SL_RDV => SL_RDV(1),
RX_READ => RX_READ(1)
);
-------------------------------------------------
INST2_SL_RX_Channel : SL_RX_Channel
generic map (
BV_MIN_ECOUNT => BV_MIN_ECOUNT )
PORT MAP( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
PBEN_I  => PBEN_I,
SCLK 	=> SCLK,
ENA_RX	=> ENA_RX(2),
SL_RX => SL_RX(2),
SLRX_CT => SLRX_CT2 ,
SL_RDV => SL_RDV(2),
RX_READ => RX_READ(2)
);
-------------------------------------------------
INST3_SL_RX_Channel : SL_RX_Channel
generic map (
BV_MIN_ECOUNT => BV_MIN_ECOUNT )
PORT MAP( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
PBEN_I  => PBEN_I,
SCLK 	=> SCLK,
ENA_RX	=> ENA_RX(3),
SL_RX => SL_RX(3),
SLRX_CT => SLRX_CT3 ,
SL_RDV => SL_RDV(3),
RX_READ => RX_READ(3)
);
-------------------------------------------------
INST4_SL_RX_Channel : SL_RX_Channel
generic map (
BV_MIN_ECOUNT => BV_MIN_ECOUNT )
PORT MAP( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
PBEN_I  => PBEN_I,
SCLK 	=> SCLK,
ENA_RX	=> ENA_RX(4),
SL_RX => SL_RX(4),
SLRX_CT => SLRX_CT4 ,
SL_RDV => SL_RDV(4),
RX_READ => RX_READ(4)
);
-------------------------------------------------
INST5_SL_RX_Channel : SL_RX_Channel
generic map (
BV_MIN_ECOUNT => BV_MIN_ECOUNT )
PORT MAP( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
PBEN_I  => PBEN_I,
SCLK 	=> SCLK,
ENA_RX	=> ENA_RX(5),
SL_RX => SL_RX(5),
SLRX_CT => SLRX_CT5 ,
SL_RDV => SL_RDV(5),
RX_READ => RX_READ(5)
);
-------------------------------------------------
INST6_SL_RX_Channel : SL_RX_Channel
generic map (
BV_MIN_ECOUNT => BV_MIN_ECOUNT )
PORT MAP( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
PBEN_I  => PBEN_I,
SCLK 	=> SCLK,
ENA_RX	=> ENA_RX(6),
SL_RX => SL_RX(6),
SLRX_CT => SLRX_CT6 ,
SL_RDV => SL_RDV(6),
RX_READ => RX_READ(6)
);
-------------------------------------------------
INST7_SL_RX_Channel : SL_RX_Channel
generic map (
BV_MIN_ECOUNT => BV_MIN_ECOUNT )
PORT MAP( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
PBEN_I  => PBEN_I,
SCLK 	=> SCLK,
ENA_RX	=> ENA_RX(7),
SL_RX => SL_RX(7),
SLRX_CT => SLRX_CT7 ,
SL_RDV => SL_RDV(7),
RX_READ => RX_READ(7)
);
-------------------------------------------------

end rtl;

