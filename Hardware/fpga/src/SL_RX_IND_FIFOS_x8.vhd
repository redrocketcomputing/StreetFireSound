----------------------------------------------------------------------------
--
--  File:   SL_RX_IND_FIFOS_x8.vhd
--  Rev:    1.0.0
--  Date:	3-4-04
--  This is the VHDL module for the 8 Independent FIFOS for recieve channels
--  for the SLINK Peripheral Module in the Streetfire RBX Companion Chip (FPGA) for 
--  Streetfire Street Racer CPU Card to Application Board Interface.  
--
--  Author: Robyn E. Bauer
--
--
--	History: 
--	Created 3-4-04 (Modified SL_TX_IND_FIFOS.vhd)
--
----------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;

library unisim;
use unisim.ALL;

entity SL_RX_IND_FIFOS_x8 is
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
end SL_RX_IND_FIFOS_x8;


architecture rtl of SL_RX_IND_FIFOS_x8 is


component RX_IND_FIFO_k
	port (
  	   clock_in:        IN  std_logic;
         read_enable_in:  IN  std_logic;
         write_enable_in: IN  std_logic;
         write_data_in:   IN  std_logic_vector(7 downto 0);
         fifo_gsr_in:     IN  std_logic;
         read_data_out:   OUT std_logic_vector(7 downto 0);
         full_out:        OUT std_logic;
         empty_out:       OUT std_logic;
         fifocount_out:   OUT std_logic_vector(7 downto 0));
end component; 

component RX_FIFO_CONTROL_k 
	generic(BV_RX_ALMOST_FULL_CT: bit_vector (7 downto 0)  ); 
	port ( 
	RST_I	:in std_logic; -- master reset for peripheral bus
	CLK_I	:in std_logic; -- master clock for peripheral bus
	ENA_RX  :in std_logic;  
	CLR_RX_FIFO : in std_logic; 
	SL_RDV : in std_logic;      
	RXFIFO_WRITE: out std_logic; --3-4	
	RXFIFO_READ: out std_logic;
	RXFIFO_EMPTY: in std_logic;
	RXFIFO_FULL: in std_logic;
	RX_READ: out std_logic;
	READ_PULSE: in std_logic;
	RXFIFO_ALMOST_FULL: out std_logic;
	RXFIFO_OVERFLOW: out std_logic;
	RXFIFO_DOUT:in std_logic_vector(7 downto 0);
	RXFIFO_COUNT:in std_logic_vector(7 downto 0);
	RCV_SL: out std_logic_vector(7 downto 0);
	RXFIFO_RE_EMPTY: out std_logic
	);
end component; 


signal RXFIFO_READ: std_logic_vector(7 downto 0);
signal RXFIFO_WRITE: std_logic_vector(7 downto 0);

signal RXFIFO_FULL: std_logic_vector(7 downto 0);
signal RXFIFO_DOUT0,
	RXFIFO_DOUT1,
	RXFIFO_DOUT2,
	RXFIFO_DOUT3,
	RXFIFO_DOUT4,
	RXFIFO_DOUT5,
	RXFIFO_DOUT6,
	RXFIFO_DOUT7 : std_logic_vector(7 downto 0);
signal RXFIFO_EMPTY: std_logic_vector(7 downto 0);
signal RXFIFO_COUNT0,
	RXFIFO_COUNT1,
	RXFIFO_COUNT2,
	RXFIFO_COUNT3,
	RXFIFO_COUNT4,
	RXFIFO_COUNT5,
	RXFIFO_COUNT6,
	RXFIFO_COUNT7 : std_logic_vector(7 downto 0);

signal FIFO_RESET:std_logic_vector(7 downto 0);



--**********************************************************
--  LOGIC BEGINS HERE:
--**********************************************************
  
begin

FIFO_RESET(0) <= RST_I or not PBEN_I or CLR_RX_FIFO(0);
FIFO_RESET(1) <= RST_I or not PBEN_I or CLR_RX_FIFO(1);
FIFO_RESET(2) <= RST_I or not PBEN_I or CLR_RX_FIFO(2);
FIFO_RESET(3) <= RST_I or not PBEN_I or CLR_RX_FIFO(3);
FIFO_RESET(4) <= RST_I or not PBEN_I or CLR_RX_FIFO(4);
FIFO_RESET(5) <= RST_I or not PBEN_I or CLR_RX_FIFO(5);
FIFO_RESET(6) <= RST_I or not PBEN_I or CLR_RX_FIFO(6);
FIFO_RESET(7) <= RST_I or not PBEN_I or CLR_RX_FIFO(7);

RXFIFO_CT0 <= RXFIFO_COUNT0;
RXFIFO_CT1 <= RXFIFO_COUNT1;
RXFIFO_CT2 <= RXFIFO_COUNT2;
RXFIFO_CT3 <= RXFIFO_COUNT3;
RXFIFO_CT4 <= RXFIFO_COUNT4;
RXFIFO_CT5 <= RXFIFO_COUNT5;
RXFIFO_CT6 <= RXFIFO_COUNT6;
RXFIFO_CT7 <= RXFIFO_COUNT7;

-- Component Instantiations:

----------------------------------------------
----------------------------------------------
INST0_RX_IND_FIFO_k : RX_IND_FIFO_k 
PORT MAP( 
clock_in => CLK_I,
read_enable_in => RXFIFO_READ(0),
write_enable_in => RXFIFO_WRITE(0),
write_data_in => SLRX_CT0,
fifo_gsr_in => FIFO_RESET(0),
read_data_out => RXFIFO_DOUT0,
full_out => RXFIFO_FULL(0),
empty_out => RXFIFO_EMPTY(0),
fifocount_out => RXFIFO_COUNT0
);
----------------------------------------------
INST0_RX_FIFO_CONTROL_k : RX_FIFO_CONTROL_k
generic map(
BV_RX_ALMOST_FULL_CT=> BV_RX_ALMOST_FULL_CT)
PORT MAP ( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
ENA_RX => ENA_RX(0),
CLR_RX_FIFO => CLR_RX_FIFO(0),
SL_RDV => SL_RDV(0),
RXFIFO_WRITE => RXFIFO_WRITE(0),
RXFIFO_READ => RXFIFO_READ(0),
RXFIFO_EMPTY => RXFIFO_EMPTY(0),
RXFIFO_FULL => RXFIFO_FULL(0),
RX_READ=> RX_READ(0),
READ_PULSE=> READ_PULSE(0),
RXFIFO_ALMOST_FULL=> RXFIFO_ALMOST_FULL(0),
RXFIFO_OVERFLOW=> RXFIFO_OVERFLOW(0),
RXFIFO_DOUT => RXFIFO_DOUT0,
RXFIFO_COUNT => RXFIFO_COUNT0,
RCV_SL=> RCV_SL0,
RXFIFO_RE_EMPTY => RXFIFO_RE_EMPTY(0)
);
----------------------------------------------
----------------------------------------------
INST1_RX_IND_FIFO_k : RX_IND_FIFO_k 
PORT MAP( 
clock_in => CLK_I,
read_enable_in => RXFIFO_READ(1),
write_enable_in => RXFIFO_WRITE(1),
write_data_in => SLRX_CT1,
fifo_gsr_in => FIFO_RESET(1),
read_data_out => RXFIFO_DOUT1,
full_out => RXFIFO_FULL(1),
empty_out => RXFIFO_EMPTY(1),
fifocount_out => RXFIFO_COUNT1
);
----------------------------------------------
INST1_RX_FIFO_CONTROL_k : RX_FIFO_CONTROL_k
generic map(
BV_RX_ALMOST_FULL_CT=> BV_RX_ALMOST_FULL_CT)
PORT MAP ( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
ENA_RX => ENA_RX(1),
CLR_RX_FIFO => CLR_RX_FIFO(1),
SL_RDV => SL_RDV(1),
RXFIFO_WRITE => RXFIFO_WRITE(1),
RXFIFO_READ => RXFIFO_READ(1),
RXFIFO_EMPTY => RXFIFO_EMPTY(1),
RXFIFO_FULL => RXFIFO_FULL(1),
RX_READ=> RX_READ(1),
READ_PULSE=> READ_PULSE(1),
RXFIFO_ALMOST_FULL=> RXFIFO_ALMOST_FULL(1),
RXFIFO_OVERFLOW=> RXFIFO_OVERFLOW(1),
RXFIFO_DOUT => RXFIFO_DOUT1,
RXFIFO_COUNT => RXFIFO_COUNT1,
RCV_SL=> RCV_SL1,
RXFIFO_RE_EMPTY => RXFIFO_RE_EMPTY(1)
);
----------------------------------------------
----------------------------------------------
INST2_RX_IND_FIFO_k : RX_IND_FIFO_k 
PORT MAP( 
clock_in => CLK_I,
read_enable_in => RXFIFO_READ(2),
write_enable_in => RXFIFO_WRITE(2),
write_data_in => SLRX_CT2,
fifo_gsr_in => FIFO_RESET(2),
read_data_out => RXFIFO_DOUT2,
full_out => RXFIFO_FULL(2),
empty_out => RXFIFO_EMPTY(2),
fifocount_out => RXFIFO_COUNT2
);
----------------------------------------------
INST2_RX_FIFO_CONTROL_k : RX_FIFO_CONTROL_k
generic map(
BV_RX_ALMOST_FULL_CT=> BV_RX_ALMOST_FULL_CT)
PORT MAP ( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
ENA_RX => ENA_RX(2),
CLR_RX_FIFO => CLR_RX_FIFO(2),
SL_RDV => SL_RDV(2),
RXFIFO_WRITE => RXFIFO_WRITE(2),
RXFIFO_READ => RXFIFO_READ(2),
RXFIFO_EMPTY => RXFIFO_EMPTY(2),
RXFIFO_FULL => RXFIFO_FULL(2),
RX_READ=> RX_READ(2),
READ_PULSE=> READ_PULSE(2),
RXFIFO_ALMOST_FULL=> RXFIFO_ALMOST_FULL(2),
RXFIFO_OVERFLOW=> RXFIFO_OVERFLOW(2),
RXFIFO_DOUT => RXFIFO_DOUT2,
RXFIFO_COUNT => RXFIFO_COUNT2,
RCV_SL=> RCV_SL2,
RXFIFO_RE_EMPTY => RXFIFO_RE_EMPTY(2)
);
----------------------------------------------
----------------------------------------------
INST3_RX_IND_FIFO_k : RX_IND_FIFO_k 
PORT MAP( 
clock_in => CLK_I,
read_enable_in => RXFIFO_READ(3),
write_enable_in => RXFIFO_WRITE(3),
write_data_in => SLRX_CT3,
fifo_gsr_in => FIFO_RESET(3),
read_data_out => RXFIFO_DOUT3,
full_out => RXFIFO_FULL(3),
empty_out => RXFIFO_EMPTY(3),
fifocount_out => RXFIFO_COUNT3
);
----------------------------------------------
INST3_RX_FIFO_CONTROL_k : RX_FIFO_CONTROL_k
generic map(
BV_RX_ALMOST_FULL_CT=> BV_RX_ALMOST_FULL_CT)
PORT MAP ( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
ENA_RX => ENA_RX(3),
CLR_RX_FIFO => CLR_RX_FIFO(3),
SL_RDV => SL_RDV(3),
RXFIFO_WRITE => RXFIFO_WRITE(3),
RXFIFO_READ => RXFIFO_READ(3),
RXFIFO_EMPTY => RXFIFO_EMPTY(3),
RXFIFO_FULL => RXFIFO_FULL(3),
RX_READ=> RX_READ(3),
READ_PULSE=> READ_PULSE(3),
RXFIFO_ALMOST_FULL=> RXFIFO_ALMOST_FULL(3),
RXFIFO_OVERFLOW=> RXFIFO_OVERFLOW(3),
RXFIFO_DOUT => RXFIFO_DOUT3,
RXFIFO_COUNT => RXFIFO_COUNT3,
RCV_SL=> RCV_SL3,
RXFIFO_RE_EMPTY => RXFIFO_RE_EMPTY(3)
);
----------------------------------------------
----------------------------------------------
INST4_RX_IND_FIFO_k : RX_IND_FIFO_k 
PORT MAP( 
clock_in => CLK_I,
read_enable_in => RXFIFO_READ(4),
write_enable_in => RXFIFO_WRITE(4),
write_data_in => SLRX_CT4,
fifo_gsr_in => FIFO_RESET(4),
read_data_out => RXFIFO_DOUT4,
full_out => RXFIFO_FULL(4),
empty_out => RXFIFO_EMPTY(4),
fifocount_out => RXFIFO_COUNT4
);
----------------------------------------------
INST4_RX_FIFO_CONTROL_k : RX_FIFO_CONTROL_k
generic map(
BV_RX_ALMOST_FULL_CT=> BV_RX_ALMOST_FULL_CT)
PORT MAP ( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
ENA_RX => ENA_RX(4),
CLR_RX_FIFO => CLR_RX_FIFO(4),
SL_RDV => SL_RDV(4),
RXFIFO_WRITE => RXFIFO_WRITE(4),
RXFIFO_READ => RXFIFO_READ(4),
RXFIFO_EMPTY => RXFIFO_EMPTY(4),
RXFIFO_FULL => RXFIFO_FULL(4),
RX_READ=> RX_READ(4),
READ_PULSE=> READ_PULSE(4),
RXFIFO_ALMOST_FULL=> RXFIFO_ALMOST_FULL(4),
RXFIFO_OVERFLOW=> RXFIFO_OVERFLOW(4),
RXFIFO_DOUT => RXFIFO_DOUT4,
RXFIFO_COUNT => RXFIFO_COUNT4,
RCV_SL=> RCV_SL4,
RXFIFO_RE_EMPTY => RXFIFO_RE_EMPTY(4)
);
----------------------------------------------
----------------------------------------------
INST5_RX_IND_FIFO_k : RX_IND_FIFO_k 
PORT MAP( 
clock_in => CLK_I,
read_enable_in => RXFIFO_READ(5),
write_enable_in => RXFIFO_WRITE(5),
write_data_in => SLRX_CT5,
fifo_gsr_in => FIFO_RESET(5),
read_data_out => RXFIFO_DOUT5,
full_out => RXFIFO_FULL(5),
empty_out => RXFIFO_EMPTY(5),
fifocount_out => RXFIFO_COUNT5
);
----------------------------------------------
INST5_RX_FIFO_CONTROL_k : RX_FIFO_CONTROL_k
generic map(
BV_RX_ALMOST_FULL_CT=> BV_RX_ALMOST_FULL_CT)
PORT MAP ( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
ENA_RX => ENA_RX(5),
CLR_RX_FIFO => CLR_RX_FIFO(5),
SL_RDV => SL_RDV(5),
RXFIFO_WRITE => RXFIFO_WRITE(5),
RXFIFO_READ => RXFIFO_READ(5),
RXFIFO_EMPTY => RXFIFO_EMPTY(5),
RXFIFO_FULL => RXFIFO_FULL(5),
RX_READ=> RX_READ(5),
READ_PULSE=> READ_PULSE(5),
RXFIFO_ALMOST_FULL=> RXFIFO_ALMOST_FULL(5),
RXFIFO_OVERFLOW=> RXFIFO_OVERFLOW(5),
RXFIFO_DOUT => RXFIFO_DOUT5,
RXFIFO_COUNT => RXFIFO_COUNT5,
RCV_SL=> RCV_SL5,
RXFIFO_RE_EMPTY => RXFIFO_RE_EMPTY(5)
);
----------------------------------------------
----------------------------------------------
INST6_RX_IND_FIFO_k : RX_IND_FIFO_k 
PORT MAP( 
clock_in => CLK_I,
read_enable_in => RXFIFO_READ(6),
write_enable_in => RXFIFO_WRITE(6),
write_data_in => SLRX_CT6,
fifo_gsr_in => FIFO_RESET(6),
read_data_out => RXFIFO_DOUT6,
full_out => RXFIFO_FULL(6),
empty_out => RXFIFO_EMPTY(6),
fifocount_out => RXFIFO_COUNT6
);
----------------------------------------------
INST6_RX_FIFO_CONTROL_k : RX_FIFO_CONTROL_k
generic map(
BV_RX_ALMOST_FULL_CT=> BV_RX_ALMOST_FULL_CT)
PORT MAP ( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
ENA_RX => ENA_RX(6),
CLR_RX_FIFO => CLR_RX_FIFO(6),
SL_RDV => SL_RDV(6),
RXFIFO_WRITE => RXFIFO_WRITE(6),
RXFIFO_READ => RXFIFO_READ(6),
RXFIFO_EMPTY => RXFIFO_EMPTY(6),
RXFIFO_FULL => RXFIFO_FULL(6),
RX_READ=> RX_READ(6),
READ_PULSE=> READ_PULSE(6),
RXFIFO_ALMOST_FULL=> RXFIFO_ALMOST_FULL(6),
RXFIFO_OVERFLOW=> RXFIFO_OVERFLOW(6),
RXFIFO_DOUT => RXFIFO_DOUT6,
RXFIFO_COUNT => RXFIFO_COUNT6,
RCV_SL=> RCV_SL6,
RXFIFO_RE_EMPTY => RXFIFO_RE_EMPTY(6)
);
----------------------------------------------
----------------------------------------------
INST7_RX_IND_FIFO_k : RX_IND_FIFO_k 
PORT MAP( 
clock_in => CLK_I,
read_enable_in => RXFIFO_READ(7),
write_enable_in => RXFIFO_WRITE(7),
write_data_in => SLRX_CT7,
fifo_gsr_in => FIFO_RESET(7),
read_data_out => RXFIFO_DOUT7,
full_out => RXFIFO_FULL(7),
empty_out => RXFIFO_EMPTY(7),
fifocount_out => RXFIFO_COUNT7
);
----------------------------------------------
INST7_RX_FIFO_CONTROL_k : RX_FIFO_CONTROL_k
generic map(
BV_RX_ALMOST_FULL_CT=> BV_RX_ALMOST_FULL_CT)
PORT MAP ( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
ENA_RX => ENA_RX(7),
CLR_RX_FIFO => CLR_RX_FIFO(7),
SL_RDV => SL_RDV(7),
RXFIFO_WRITE => RXFIFO_WRITE(7),
RXFIFO_READ => RXFIFO_READ(7),
RXFIFO_EMPTY => RXFIFO_EMPTY(7),
RXFIFO_FULL => RXFIFO_FULL(7),
RX_READ=> RX_READ(7),
READ_PULSE=> READ_PULSE(7),
RXFIFO_ALMOST_FULL=> RXFIFO_ALMOST_FULL(7),
RXFIFO_OVERFLOW=> RXFIFO_OVERFLOW(7),
RXFIFO_DOUT => RXFIFO_DOUT7,
RXFIFO_COUNT => RXFIFO_COUNT7,
RCV_SL=> RCV_SL7,
RXFIFO_RE_EMPTY => RXFIFO_RE_EMPTY(7)
);
----------------------------------------------

end rtl;

