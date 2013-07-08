----------------------------------------------------------------------------
--
--  File:   SL_Transmitter_x8.vhd
--  Rev:    1.0.0
--  Date:	2-27-04
--  This is the Wrapper VHDL module for the Kth SLINK Transmitter for the SLINK Peripheral Module
-- in the Streetfire RBX Companion Chip (FPGA) for Streetfire Street Racer CPU Card to 
--  Application Board Interface.  
--
--  Author: Robyn E. Bauer
--
--  Restrictions: 
--  1.  Do not use send count of less than 2, except for end of message count of 0
--  2.  Do not issue an initial send count of 0 (or 1).
--	
--
--	History: 
--	Created 2-27-04 Modified from Transmit_k_FIFO.vhd
----------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;

library unisim;
use unisim.ALL;

entity SL_Transmitter_x8 is
	generic(BV_DCOUNT,BV_ECOUNT,BV_TX_ALMOST_EMPTY_CT: bit_vector (7 downto 0)); 
	port ( 
	RST_I		:in std_logic; -- master reset for peripheral bus
	CLK_I		:in std_logic; -- master clock for peripheral bus
	PBEN_I  	:in std_logic;
	SCLK		:in std_logic; -- SLINK CLock (timebase of 10us)
	ENA_TX	:in std_logic_vector(7 downto 0);  -- SLINK Enable from control register
	SL_XDV	:in std_logic_vector(7 downto 0);  -- SLINK Transmit data valid (count ready to transmit)
	SL_TX		:out std_logic_vector(7 downto 0);  -- SLINK TX signal
	INT_TS	:out std_logic_vector(7 downto 0);   -- Transmit Service Requested interrupt output(active high)
	STAT_TF	:out std_logic_vector(7 downto 0);   -- TX FIFO FULL STATUS
	CLR_TX_FIFO:in std_logic_vector(7 downto 0);  -- Active high synchronous FIFO Clear	
	SL_RX 	: in std_logic_vector(7 downto 0); -- SLINK RX input
	INT_TC	:out std_logic_vector(7 downto 0);  -- TX collision interrupt	
	SEND_SL0,
	SEND_SL1,
	SEND_SL2,
	SEND_SL3,
	SEND_SL4,
	SEND_SL5,
	SEND_SL6,
	SEND_SL7
		:in std_logic_vector(7 downto 0); -- Transmit Send Count (low time) from reg.
	TXFIFO_CT0,
	TXFIFO_CT1,
	TXFIFO_CT2,
	TXFIFO_CT3,
	TXFIFO_CT4,
	TXFIFO_CT5,
	TXFIFO_CT6,
	TXFIFO_CT7
		:out std_logic_vector(7 downto 0)
	);
end SL_Transmitter_x8;


architecture rtl of SL_Transmitter_x8 is


component SL_TX_Channel 
	generic(BV_DCOUNT,BV_ECOUNT: bit_vector (7 downto 0)); 
	port ( 
	RST_I	:in std_logic; -- master reset for peripheral bus
	CLK_I	:in std_logic; -- master clock for peripheral bus
	PBEN_I  :in std_logic;
	SCLK	:in std_logic; -- SLINK CLock (timebase of 10us)
	ENA_TX	:in std_logic; -- SLINK Enable from control register
	SL_TDV	:in std_logic; -- SLINK Transmit data valid (count ready to transmit)
	SLTX_CT :in std_logic_vector(7 downto 0); -- Transmit Send Count (low time) from reg.
	TX_READY :out std_logic; -- indicates NEXT tx value CAN be WRITTEN TO register
	SL_TX	:out std_logic; -- SLINK TX signal
	SL_RX 	: in std_logic; -- SLINK RX input
	SL_TC	:out std_logic -- Transmit Collision Detected 
	);
end component; -- SL_TX_Channel


signal TX_READY:std_logic_vector(7 downto 0);


--**********************************************************
--  LOGIC BEGINS HERE:
--**********************************************************
  
begin

INT_TS <= TX_READY;  -- for non-fifo version, service request is when ready to write nextdata
STAT_TF <= (others => '0');  -- RESERVED FOR TX FIFO FULL FLAG
TXFIFO_CT0 <= (others => '0');  -- RESERVED FOR TX FIFO COUNT
TXFIFO_CT1 <= (others => '0');  -- RESERVED FOR TX FIFO COUNT
TXFIFO_CT2 <= (others => '0');  -- RESERVED FOR TX FIFO COUNT
TXFIFO_CT3 <= (others => '0');  -- RESERVED FOR TX FIFO COUNT
TXFIFO_CT4 <= (others => '0');  -- RESERVED FOR TX FIFO COUNT
TXFIFO_CT5 <= (others => '0');  -- RESERVED FOR TX FIFO COUNT
TXFIFO_CT6 <= (others => '0');  -- RESERVED FOR TX FIFO COUNT
TXFIFO_CT7 <= (others => '0');  -- RESERVED FOR TX FIFO COUNT



-- Component Instantiations:

----------------------------------------------
INST0_SL_TX_Channel : SL_TX_Channel 
generic map (
BV_DCOUNT => BV_DCOUNT,
BV_ECOUNT => BV_ECOUNT )
PORT MAP( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
PBEN_I  => PBEN_I,
SCLK 	=> SCLK,
ENA_TX	=> ENA_TX(0),
SL_TDV	=> SL_XDV(0),
SLTX_CT => SEND_SL0,
TX_READY => TX_READY(0),
SL_TX	=> SL_TX(0),
SL_RX => SL_RX(0),
SL_TC => INT_TC(0)
);
----------------------------------------------
INST1_SL_TX_Channel : SL_TX_Channel 
generic map (
BV_DCOUNT => BV_DCOUNT,
BV_ECOUNT => BV_ECOUNT )
PORT MAP( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
PBEN_I  => PBEN_I,
SCLK 	=> SCLK,
ENA_TX	=> ENA_TX(1),
SL_TDV	=> SL_XDV(1),
SLTX_CT => SEND_SL1,
TX_READY => TX_READY(1),
SL_TX	=> SL_TX(1),
SL_RX => SL_RX(1),
SL_TC => INT_TC(1)
);
----------------------------------------------
INST2_SL_TX_Channel : SL_TX_Channel 
generic map (
BV_DCOUNT => BV_DCOUNT,
BV_ECOUNT => BV_ECOUNT )
PORT MAP( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
PBEN_I  => PBEN_I,
SCLK 	=> SCLK,
ENA_TX	=> ENA_TX(2),
SL_TDV	=> SL_XDV(2),
SLTX_CT => SEND_SL2,
TX_READY => TX_READY(2),
SL_TX	=> SL_TX(2),
SL_RX => SL_RX(2),
SL_TC => INT_TC(2)
);
----------------------------------------------
INST3_SL_TX_Channel : SL_TX_Channel 
generic map (
BV_DCOUNT => BV_DCOUNT,
BV_ECOUNT => BV_ECOUNT )
PORT MAP( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
PBEN_I  => PBEN_I,
SCLK 	=> SCLK,
ENA_TX	=> ENA_TX(3),
SL_TDV	=> SL_XDV(3),
SLTX_CT => SEND_SL3,
TX_READY => TX_READY(3),
SL_TX	=> SL_TX(3),
SL_RX => SL_RX(3),
SL_TC => INT_TC(3)
);
----------------------------------------------
INST4_SL_TX_Channel : SL_TX_Channel 
generic map (
BV_DCOUNT => BV_DCOUNT,
BV_ECOUNT => BV_ECOUNT )
PORT MAP( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
PBEN_I  => PBEN_I,
SCLK 	=> SCLK,
ENA_TX	=> ENA_TX(4),
SL_TDV	=> SL_XDV(4),
SLTX_CT => SEND_SL4,
TX_READY => TX_READY(4),
SL_TX	=> SL_TX(4),
SL_RX => SL_RX(4),
SL_TC => INT_TC(4)
);
----------------------------------------------
INST5_SL_TX_Channel : SL_TX_Channel 
generic map (
BV_DCOUNT => BV_DCOUNT,
BV_ECOUNT => BV_ECOUNT )
PORT MAP( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
PBEN_I  => PBEN_I,
SCLK 	=> SCLK,
ENA_TX	=> ENA_TX(5),
SL_TDV	=> SL_XDV(5),
SLTX_CT => SEND_SL5,
TX_READY => TX_READY(5),
SL_TX	=> SL_TX(5),
SL_RX => SL_RX(5),
SL_TC => INT_TC(5)
);
----------------------------------------------
INST6_SL_TX_Channel : SL_TX_Channel 
generic map (
BV_DCOUNT => BV_DCOUNT,
BV_ECOUNT => BV_ECOUNT )
PORT MAP( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
PBEN_I  => PBEN_I,
SCLK 	=> SCLK,
ENA_TX	=> ENA_TX(6),
SL_TDV	=> SL_XDV(6),
SLTX_CT => SEND_SL6,
TX_READY => TX_READY(6),
SL_TX	=> SL_TX(6),
SL_RX => SL_RX(6),
SL_TC => INT_TC(6)
);
----------------------------------------------
INST7_SL_TX_Channel : SL_TX_Channel 
generic map (
BV_DCOUNT => BV_DCOUNT,
BV_ECOUNT => BV_ECOUNT )
PORT MAP( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
PBEN_I  => PBEN_I,
SCLK 	=> SCLK,
ENA_TX	=> ENA_TX(7),
SL_TDV	=> SL_XDV(7),
SLTX_CT => SEND_SL7,
TX_READY => TX_READY(7),
SL_TX	=> SL_TX(7),
SL_RX => SL_RX(7),
SL_TC => INT_TC(7)
);
----------------------------------------------


end rtl;

