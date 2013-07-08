----------------------------------------------------------------------------
--
--  File:   SL_TX_IND_FIFOS_x8.vhd
--  Rev:    1.0.0
--  Date:	3-1-04
--  This is the VHDL module for the 8 Independent FIFOS for transmit channels
--  for the SLINK Peripheral Module in the Streetfire RBX Companion Chip (FPGA) for 
--  Streetfire Street Racer CPU Card to Application Board Interface.  
--
--  Author: Robyn E. Bauer
--
--
--	History: 
--	Created 3-1-04 
--	3-4-04 Replaced TX_FIFO_READER_k with TX_FIFO_CONTROL_k module (for state controlled fifo writes)
--
----------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;

library unisim;
use unisim.ALL;

entity SL_TX_IND_FIFOS_x8 is
	generic(BV_TX_ALMOST_EMPTY_CT: bit_vector (7 downto 0)); 
	port ( 
	RST_I	:in std_logic; -- master reset for peripheral bus
	CLK_I	:in std_logic; -- master clock for peripheral bus
	PBEN_I  :in std_logic;
	ENA_TX  :in std_logic_vector(7 downto 0);  -- SLINK Enable from control register
	SL_XDV :in std_logic_vector(7 downto 0); 
	CLR_TX_FIFO :in std_logic_vector(7 downto 0);  
	SEND_SL0,
	SEND_SL1,
	SEND_SL2,
	SEND_SL3,
	SEND_SL4,
	SEND_SL5,
	SEND_SL6,
	SEND_SL7 :in std_logic_vector(7 downto 0); -- Transmit Send Count (low time) from reg.
	SL_TDV : out std_logic_vector(7 downto 0);
	SLTX_CT0,
	SLTX_CT1,
	SLTX_CT2,
	SLTX_CT3,
	SLTX_CT4,
	SLTX_CT5,
	SLTX_CT6,
	SLTX_CT7 : out std_logic_vector(7 downto 0);
	TX_READY : in std_logic_vector(7 downto 0);
	TXFIFO_ALMOST_EMPTY : out std_logic_vector(7 downto 0);
	TXFIFO_FULL : out std_logic_vector(7 downto 0);
	TXFIFO_CT0,
	TXFIFO_CT1,
	TXFIFO_CT2,
	TXFIFO_CT3,
	TXFIFO_CT4,
	TXFIFO_CT5,
	TXFIFO_CT6,
	TXFIFO_CT7 : out std_logic_vector(7 downto 0)
	);
end SL_TX_IND_FIFOS_x8;


architecture rtl of SL_TX_IND_FIFOS_x8 is


component TX_IND_FIFO_k
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

component TX_FIFO_CONTROL_k 
	generic(BV_TX_ALMOST_EMPTY_CT: bit_vector (7 downto 0)); 
	port ( 
	RST_I	:in std_logic; -- master reset for peripheral bus
	CLK_I	:in std_logic; -- master clock for peripheral bus
	ENA_TX  :in std_logic;  
	CLR_TX_FIFO : in std_logic; --3-4
	SL_XDV : in std_logic;      --3-4
	TXFIFO_WRITE: out std_logic; --3-4	
	TXFIFO_READ: out std_logic;
	TXFIFO_EMPTY: in std_logic;
	TXFIFO_FULL_OUT: in std_logic;
	SL_TDV: out std_logic;
	TX_READY: in std_logic;
	TXFIFO_ALMOST_EMPTY: out std_logic;
	TXFIFO_FULL: out std_logic;
	TXFIFO_DOUT:in std_logic_vector(7 downto 0);
	TXFIFO_COUNT:in std_logic_vector(7 downto 0);
	SLTX_CT: out std_logic_vector(7 downto 0)
	);
end component; 


signal TXFIFO_READ: std_logic_vector(7 downto 0);
signal TXFIFO_EMPTY: std_logic_vector(7 downto 0);
signal TXFIFO_FULL_OUT: std_logic_vector(7 downto 0);
signal TXFIFO_DOUT0,
	TXFIFO_DOUT1,
	TXFIFO_DOUT2,
	TXFIFO_DOUT3,
	TXFIFO_DOUT4,
	TXFIFO_DOUT5,
	TXFIFO_DOUT6,
	TXFIFO_DOUT7 : std_logic_vector(7 downto 0);
signal TXFIFO_COUNT0,
	TXFIFO_COUNT1,
	TXFIFO_COUNT2,
	TXFIFO_COUNT3,
	TXFIFO_COUNT4,
	TXFIFO_COUNT5,
	TXFIFO_COUNT6,
	TXFIFO_COUNT7 : std_logic_vector(7 downto 0);

signal FIFO_RESET:std_logic_vector(7 downto 0);

signal TXFIFO_WRITE: std_logic_vector(7 downto 0);

--**********************************************************
--  LOGIC BEGINS HERE:
--**********************************************************
  
begin

FIFO_RESET(0) <= RST_I or not PBEN_I or CLR_TX_FIFO(0);
FIFO_RESET(1) <= RST_I or not PBEN_I or CLR_TX_FIFO(1);
FIFO_RESET(2) <= RST_I or not PBEN_I or CLR_TX_FIFO(2);
FIFO_RESET(3) <= RST_I or not PBEN_I or CLR_TX_FIFO(3);
FIFO_RESET(4) <= RST_I or not PBEN_I or CLR_TX_FIFO(4);
FIFO_RESET(5) <= RST_I or not PBEN_I or CLR_TX_FIFO(5);
FIFO_RESET(6) <= RST_I or not PBEN_I or CLR_TX_FIFO(6);
FIFO_RESET(7) <= RST_I or not PBEN_I or CLR_TX_FIFO(7);

TXFIFO_CT0 <= TXFIFO_COUNT0;
TXFIFO_CT1 <= TXFIFO_COUNT1;
TXFIFO_CT2 <= TXFIFO_COUNT2;
TXFIFO_CT3 <= TXFIFO_COUNT3;
TXFIFO_CT4 <= TXFIFO_COUNT4;
TXFIFO_CT5 <= TXFIFO_COUNT5;
TXFIFO_CT6 <= TXFIFO_COUNT6;
TXFIFO_CT7 <= TXFIFO_COUNT7;

-- Component Instantiations:

----------------------------------------------
----------------------------------------------
INST0_TX_IND_FIFO_k : TX_IND_FIFO_k 
PORT MAP( 
clock_in => CLK_I,
read_enable_in => TXFIFO_READ(0),
write_enable_in => TXFIFO_WRITE(0),
write_data_in => SEND_SL0,
fifo_gsr_in => FIFO_RESET(0),
read_data_out => TXFIFO_DOUT0,
full_out => TXFIFO_FULL_OUT(0),
empty_out => TXFIFO_EMPTY(0),
fifocount_out => TXFIFO_COUNT0
);
----------------------------------------------
INST0_TX_FIFO_CONTROL_k : TX_FIFO_CONTROL_k
generic map(
BV_TX_ALMOST_EMPTY_CT => BV_TX_ALMOST_EMPTY_CT)
PORT MAP ( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
ENA_TX => ENA_TX(0),
CLR_TX_FIFO => CLR_TX_FIFO(0),
SL_XDV => SL_XDV(0),
TXFIFO_WRITE => TXFIFO_WRITE(0),
TXFIFO_READ => TXFIFO_READ(0),
TXFIFO_EMPTY => TXFIFO_EMPTY(0),
TXFIFO_FULL_OUT => TXFIFO_FULL_OUT(0),
SL_TDV => SL_TDV(0),
TX_READY => TX_READY(0),
TXFIFO_ALMOST_EMPTY => TXFIFO_ALMOST_EMPTY(0),
TXFIFO_FULL => TXFIFO_FULL(0),
TXFIFO_DOUT => TXFIFO_DOUT0,
TXFIFO_COUNT => TXFIFO_COUNT0,
SLTX_CT => SLTX_CT0
);
----------------------------------------------
----------------------------------------------
INST1_TX_IND_FIFO_k : TX_IND_FIFO_k 
PORT MAP( 
clock_in => CLK_I,
read_enable_in => TXFIFO_READ(1),
write_enable_in => TXFIFO_WRITE(1),
write_data_in => SEND_SL1,
fifo_gsr_in => FIFO_RESET(1),
read_data_out => TXFIFO_DOUT1,
full_out => TXFIFO_FULL_OUT(1),
empty_out => TXFIFO_EMPTY(1),
fifocount_out => TXFIFO_COUNT1
);
----------------------------------------------
INST1_TX_FIFO_CONTROL_k : TX_FIFO_CONTROL_k 
generic map(
BV_TX_ALMOST_EMPTY_CT => BV_TX_ALMOST_EMPTY_CT)
PORT MAP ( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
ENA_TX => ENA_TX(1),
CLR_TX_FIFO => CLR_TX_FIFO(1),
SL_XDV => SL_XDV(1),
TXFIFO_WRITE => TXFIFO_WRITE(1),
TXFIFO_READ => TXFIFO_READ(1),
TXFIFO_EMPTY => TXFIFO_EMPTY(1),
TXFIFO_FULL_OUT => TXFIFO_FULL_OUT(1),
SL_TDV => SL_TDV(1),
TX_READY => TX_READY(1),
TXFIFO_ALMOST_EMPTY => TXFIFO_ALMOST_EMPTY(1),
TXFIFO_FULL => TXFIFO_FULL(1),
TXFIFO_DOUT => TXFIFO_DOUT1,
TXFIFO_COUNT => TXFIFO_COUNT1,
SLTX_CT => SLTX_CT1
);
----------------------------------------------
----------------------------------------------
INST2_TX_IND_FIFO_k : TX_IND_FIFO_k 
PORT MAP( 
clock_in => CLK_I,
read_enable_in => TXFIFO_READ(2),
write_enable_in => TXFIFO_WRITE(2),
write_data_in => SEND_SL2,
fifo_gsr_in => FIFO_RESET(2),
read_data_out => TXFIFO_DOUT2,
full_out => TXFIFO_FULL_OUT(2),
empty_out => TXFIFO_EMPTY(2),
fifocount_out => TXFIFO_COUNT2
);
----------------------------------------------
INST2_TX_FIFO_CONTROL_k : TX_FIFO_CONTROL_k 
generic map(
BV_TX_ALMOST_EMPTY_CT => BV_TX_ALMOST_EMPTY_CT)
PORT MAP ( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
ENA_TX => ENA_TX(2),
CLR_TX_FIFO => CLR_TX_FIFO(2),
SL_XDV => SL_XDV(2),
TXFIFO_WRITE => TXFIFO_WRITE(2),
TXFIFO_READ => TXFIFO_READ(2),
TXFIFO_EMPTY => TXFIFO_EMPTY(2),
TXFIFO_FULL_OUT => TXFIFO_FULL_OUT(2),
SL_TDV => SL_TDV(2),
TX_READY => TX_READY(2),
TXFIFO_ALMOST_EMPTY => TXFIFO_ALMOST_EMPTY(2),
TXFIFO_FULL => TXFIFO_FULL(2),
TXFIFO_DOUT => TXFIFO_DOUT2,
TXFIFO_COUNT => TXFIFO_COUNT2,
SLTX_CT => SLTX_CT2
);
----------------------------------------------
----------------------------------------------
INST3_TX_IND_FIFO_k : TX_IND_FIFO_k 
PORT MAP( 
clock_in => CLK_I,
read_enable_in => TXFIFO_READ(3),
write_enable_in => TXFIFO_WRITE(3),
write_data_in => SEND_SL3,
fifo_gsr_in => FIFO_RESET(3),
read_data_out => TXFIFO_DOUT3,
full_out => TXFIFO_FULL_OUT(3),
empty_out => TXFIFO_EMPTY(3),
fifocount_out => TXFIFO_COUNT3
);
----------------------------------------------
INST3_TX_FIFO_CONTROL_k : TX_FIFO_CONTROL_k 
generic map(
BV_TX_ALMOST_EMPTY_CT => BV_TX_ALMOST_EMPTY_CT)
PORT MAP ( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
ENA_TX => ENA_TX(3),
CLR_TX_FIFO => CLR_TX_FIFO(3),
SL_XDV => SL_XDV(3),
TXFIFO_WRITE => TXFIFO_WRITE(3),
TXFIFO_READ => TXFIFO_READ(3),
TXFIFO_EMPTY => TXFIFO_EMPTY(3),
TXFIFO_FULL_OUT => TXFIFO_FULL_OUT(3),
SL_TDV => SL_TDV(3),
TX_READY => TX_READY(3),
TXFIFO_ALMOST_EMPTY => TXFIFO_ALMOST_EMPTY(3),
TXFIFO_FULL => TXFIFO_FULL(3),
TXFIFO_DOUT => TXFIFO_DOUT3,
TXFIFO_COUNT => TXFIFO_COUNT3,
SLTX_CT => SLTX_CT3
);
----------------------------------------------
----------------------------------------------
INST4_TX_IND_FIFO_k : TX_IND_FIFO_k 
PORT MAP( 
clock_in => CLK_I,
read_enable_in => TXFIFO_READ(4),
write_enable_in => TXFIFO_WRITE(4),
write_data_in => SEND_SL4,
fifo_gsr_in => FIFO_RESET(4),
read_data_out => TXFIFO_DOUT4,
full_out => TXFIFO_FULL_OUT(4),
empty_out => TXFIFO_EMPTY(4),
fifocount_out => TXFIFO_COUNT4
);
----------------------------------------------
INST4_TX_FIFO_CONTROL_k : TX_FIFO_CONTROL_k 
generic map(
BV_TX_ALMOST_EMPTY_CT => BV_TX_ALMOST_EMPTY_CT)
PORT MAP ( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
ENA_TX => ENA_TX(4),
CLR_TX_FIFO => CLR_TX_FIFO(4),
SL_XDV => SL_XDV(4),
TXFIFO_WRITE => TXFIFO_WRITE(4),
TXFIFO_READ => TXFIFO_READ(4),
TXFIFO_EMPTY => TXFIFO_EMPTY(4),
TXFIFO_FULL_OUT => TXFIFO_FULL_OUT(4),
SL_TDV => SL_TDV(4),
TX_READY => TX_READY(4),
TXFIFO_ALMOST_EMPTY => TXFIFO_ALMOST_EMPTY(4),
TXFIFO_FULL => TXFIFO_FULL(4),
TXFIFO_DOUT => TXFIFO_DOUT4,
TXFIFO_COUNT => TXFIFO_COUNT4,
SLTX_CT => SLTX_CT4
);
----------------------------------------------
----------------------------------------------
INST5_TX_IND_FIFO_k : TX_IND_FIFO_k 
PORT MAP( 
clock_in => CLK_I,
read_enable_in => TXFIFO_READ(5),
write_enable_in => TXFIFO_WRITE(5),
write_data_in => SEND_SL5,
fifo_gsr_in => FIFO_RESET(5),
read_data_out => TXFIFO_DOUT5,
full_out => TXFIFO_FULL_OUT(5),
empty_out => TXFIFO_EMPTY(5),
fifocount_out => TXFIFO_COUNT5
);
----------------------------------------------
INST5_TX_FIFO_CONTROL_k : TX_FIFO_CONTROL_k 
generic map(
BV_TX_ALMOST_EMPTY_CT => BV_TX_ALMOST_EMPTY_CT)
PORT MAP ( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
ENA_TX => ENA_TX(5),
CLR_TX_FIFO => CLR_TX_FIFO(5),
SL_XDV => SL_XDV(5),
TXFIFO_WRITE => TXFIFO_WRITE(5),
TXFIFO_READ => TXFIFO_READ(5),
TXFIFO_EMPTY => TXFIFO_EMPTY(5),
TXFIFO_FULL_OUT => TXFIFO_FULL_OUT(5),
SL_TDV => SL_TDV(5),
TX_READY => TX_READY(5),
TXFIFO_ALMOST_EMPTY => TXFIFO_ALMOST_EMPTY(5),
TXFIFO_FULL => TXFIFO_FULL(5),
TXFIFO_DOUT => TXFIFO_DOUT5,
TXFIFO_COUNT => TXFIFO_COUNT5,
SLTX_CT => SLTX_CT5
);
----------------------------------------------
----------------------------------------------
INST6_TX_IND_FIFO_k : TX_IND_FIFO_k 
PORT MAP( 
clock_in => CLK_I,
read_enable_in => TXFIFO_READ(6),
write_enable_in => TXFIFO_WRITE(6),
write_data_in => SEND_SL6,
fifo_gsr_in => FIFO_RESET(6),
read_data_out => TXFIFO_DOUT6,
full_out => TXFIFO_FULL_OUT(6),
empty_out => TXFIFO_EMPTY(6),
fifocount_out => TXFIFO_COUNT6
);
----------------------------------------------
INST6_TX_FIFO_CONTROL_k : TX_FIFO_CONTROL_k 
generic map(
BV_TX_ALMOST_EMPTY_CT => BV_TX_ALMOST_EMPTY_CT)
PORT MAP ( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
ENA_TX => ENA_TX(6),
CLR_TX_FIFO => CLR_TX_FIFO(6),
SL_XDV => SL_XDV(6),
TXFIFO_WRITE => TXFIFO_WRITE(6),
TXFIFO_READ => TXFIFO_READ(6),
TXFIFO_EMPTY => TXFIFO_EMPTY(6),
TXFIFO_FULL_OUT => TXFIFO_FULL_OUT(6),
SL_TDV => SL_TDV(6),
TX_READY => TX_READY(6),
TXFIFO_ALMOST_EMPTY => TXFIFO_ALMOST_EMPTY(6),
TXFIFO_FULL => TXFIFO_FULL(6),
TXFIFO_DOUT => TXFIFO_DOUT6,
TXFIFO_COUNT => TXFIFO_COUNT6,
SLTX_CT => SLTX_CT6
);
----------------------------------------------
----------------------------------------------
INST7_TX_IND_FIFO_k : TX_IND_FIFO_k 
PORT MAP( 
clock_in => CLK_I,
read_enable_in => TXFIFO_READ(7),
write_enable_in => TXFIFO_WRITE(7),
write_data_in => SEND_SL7,
fifo_gsr_in => FIFO_RESET(7),
read_data_out => TXFIFO_DOUT7,
full_out => TXFIFO_FULL_OUT(7),
empty_out => TXFIFO_EMPTY(7),
fifocount_out => TXFIFO_COUNT7
);
----------------------------------------------
INST7_TX_FIFO_CONTROL_k : TX_FIFO_CONTROL_k 
generic map(
BV_TX_ALMOST_EMPTY_CT => BV_TX_ALMOST_EMPTY_CT)
PORT MAP ( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
ENA_TX => ENA_TX(7),
CLR_TX_FIFO => CLR_TX_FIFO(7),
SL_XDV => SL_XDV(7),
TXFIFO_WRITE => TXFIFO_WRITE(7),
TXFIFO_READ => TXFIFO_READ(7),
TXFIFO_EMPTY => TXFIFO_EMPTY(7),
TXFIFO_FULL_OUT => TXFIFO_FULL_OUT(7),
SL_TDV => SL_TDV(7),
TX_READY => TX_READY(7),
TXFIFO_ALMOST_EMPTY => TXFIFO_ALMOST_EMPTY(7),
TXFIFO_FULL => TXFIFO_FULL(7),
TXFIFO_DOUT => TXFIFO_DOUT7,
TXFIFO_COUNT => TXFIFO_COUNT7,
SLTX_CT => SLTX_CT7
);

end rtl;

