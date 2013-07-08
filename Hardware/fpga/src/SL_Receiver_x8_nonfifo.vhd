----------------------------------------------------------------------------
--
--  File:   SL_Receiver_x8.vhd
--  Rev:    1.0.0
--  Date:	2-27-04
--  This is the Wrapper VHDL module for the 8 SLINK Receivers for the SLINK Peripheral Module
-- in the Streetfire RBX Companion Chip (FPGA) for Streetfire Street Racer CPU Card to 
--  Application Board Interface
--
--  Author: Robyn E. Bauer
--
--	History: 
--	
--	Created 2-27-04 (modified from Receive_k_FIFO.vhd)
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

-- SIGNAL DEFINITIONS:

signal SL_RDV_out:std_logic_vector(7 downto 0);

--**********************************************************
--  LOGIC BEGINS HERE:
--**********************************************************
  
begin

INT_RS 	<= SL_RDV_out ; -- RCV Request Service flag (non fifo-assign to SL_RDV)
STAT_RO 	<= (others => '0'); -- RCV FIFO OVerflow flag
STAT_RE	<= (others => '0'); -- RCV FIFO Empty flag
RXFIFO_CT0 <= (others => '0');
RXFIFO_CT1 <= (others => '0');
RXFIFO_CT2 <= (others => '0');
RXFIFO_CT3 <= (others => '0');
RXFIFO_CT4 <= (others => '0');
RXFIFO_CT5 <= (others => '0');
RXFIFO_CT6 <= (others => '0');
RXFIFO_CT7 <= (others => '0');

-- Component Instantiations:
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
SLRX_CT => RCV_SL0 ,
SL_RDV => SL_RDV_out(0),
RX_READ => READ_PULSE(0)
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
SLRX_CT => RCV_SL1 ,
SL_RDV => SL_RDV_out(1),
RX_READ => READ_PULSE(1)
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
SLRX_CT => RCV_SL2 ,
SL_RDV => SL_RDV_out(2),
RX_READ => READ_PULSE(2)
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
SLRX_CT => RCV_SL3 ,
SL_RDV => SL_RDV_out(3),
RX_READ => READ_PULSE(3)
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
SLRX_CT => RCV_SL4 ,
SL_RDV => SL_RDV_out(4),
RX_READ => READ_PULSE(4)
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
SLRX_CT => RCV_SL5 ,
SL_RDV => SL_RDV_out(5),
RX_READ => READ_PULSE(5)
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
SLRX_CT => RCV_SL6 ,
SL_RDV => SL_RDV_out(6),
RX_READ => READ_PULSE(6)
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
SLRX_CT => RCV_SL7 ,
SL_RDV => SL_RDV_out(7),
RX_READ => READ_PULSE(7)
);
-------------------------------------------------

end rtl;

