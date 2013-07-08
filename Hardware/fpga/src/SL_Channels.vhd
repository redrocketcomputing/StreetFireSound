----------------------------------------------------------------------------
--
--  File:   SL_Channels.vhd
--  Rev:    1.0.0
--  Date:	3-5-04
--  This is the VHDL module for the 8-Channel SLINK Transmitter for the SLINK Peripheral Module
-- in the Streetfire RBX Companion Chip (FPGA) for Streetfire Street Racer CPU Card to 
--  Application Board Interface
--
--  Author: Robyn E. Bauer
--
--  Restrictions: 
--  1.  Do not use send count of less than 2, except for end of message count of 0
--  2.  Do not issue an initial send count of 0 (or 1).
--	
--
--	History: 
--	Created 2-27-04 modified from Transmitter_x8.vhd
--	3-5-04 Increased RX_ALMOST_FULL_CT to 95 (x"5F") and TX_ALMOST_EMPTY_CT to 32 (x"20"),
--		per S. Street
--	
----------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;

library unisim;
use unisim.ALL;

entity SL_Channels is 
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
end SL_Channels;


architecture rtl of SL_Channels is

------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------
-- USER DEFINED SLINK VARIABLES:
------------------------------------------------------------------------------------------------

-- RX FIFO ALMOST FULL COUNT:
-- Desired level is 95 (x5F)
constant BV_RX_ALMOST_FULL_CT: bit_vector (7 downto 0) := "01011111"; -- set to desired level

-- simulation level is 3
--constant BV_RX_ALMOST_FULL_CT: bit_vector (7 downto 0) := "00000011"; -- set to 3 for simulation

-- TX FIFO ALMOST EMPTY COUNT:
-- Desired level is 32 (x20)
constant BV_TX_ALMOST_EMPTY_CT: bit_vector (7 downto 0) := "00100000"; -- set to desired level

-- simulation level is 3
--constant BV_TX_ALMOST_EMPTY_CT: bit_vector (7 downto 0) := "00000011"; -- set to 3 for simulation


------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------



constant BV_DCOUNT: bit_vector (7 downto 0) := "00111100"; -- Delimiter Count =60 (600us/10us)
constant BV_ECOUNT: bit_vector (7 downto 0) := "00111100"; -- End Count =60 (600us/10us)
constant BV_MIN_ECOUNT: bit_vector (7 downto 0) := "01101110"; -- Minimum End Count  = 110 (10 less than used)
constant BV_IDLE_COUNT: bit_vector (7 downto 0) := "10110100"; -- Idle Count  = 3*60=180  


-- SIGNAL DEFINITIONS:

-- Components


component SL_Transmitter_x8
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
end component; --SL_Transmitter


component SL_Receiver_x8
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
end component; --SL_Receiver

component SL_Line_Status_x8 
	generic(BV_IDLE_COUNT: bit_vector (7 downto 0)  ); --Idle count in CLK_I's
	port ( 
	RST_I	:in std_logic; -- master reset for peripheral bus
	CLK_I	:in std_logic; -- master clock for peripheral bus
	PBEN_I  : in std_logic;
	SCLK	:in std_logic; -- SLINK CLock (timebase of 10us)
	SL_RX	: in std_logic_vector(7 downto 0); -- SLINK RX signal
	INT_LI	:out std_logic_vector(7 downto 0)  -- INDICATES LINE IS IDLE (AFTER 3 DELIMITER TIMES)
);
end component; --SL_Line_Status_x8



--------------------------------------------------------------

--**********************************************************
--  LOGIC BEGINS HERE:
--**********************************************************
begin

-- Port Maps
--------------------------------------------------------------
INST_SL_Transmitter_x8 : SL_Transmitter_x8
generic map (
BV_DCOUNT => BV_DCOUNT,
BV_ECOUNT => BV_ECOUNT,
BV_TX_ALMOST_EMPTY_CT=>BV_TX_ALMOST_EMPTY_CT )
PORT MAP( 
RST_I		=> RST_I,
CLK_I		=> CLK_I,
PBEN_I  	=> PBEN_I,
SCLK 		=> SCLK,
ENA_TX	=> ENA_TX,
SL_XDV	=> SL_XDV,
SL_TX		=> SL_TX,
INT_TS	=> INT_TS,
STAT_TF	=> STAT_TF,
CLR_TX_FIFO => CLR_TX_FIFO,
SL_RX 	=> SL_RX,
INT_TC  	=> INT_TC,
SEND_SL0	=> SEND_SL0,
SEND_SL1	=> SEND_SL1,
SEND_SL2	=> SEND_SL2,
SEND_SL3	=> SEND_SL3,
SEND_SL4	=> SEND_SL4,
SEND_SL5	=> SEND_SL5,
SEND_SL6	=> SEND_SL6,
SEND_SL7	=> SEND_SL7,
TXFIFO_CT0 	=> TXFIFO_CT0,
TXFIFO_CT1 	=> TXFIFO_CT1,
TXFIFO_CT2 	=> TXFIFO_CT2,
TXFIFO_CT3 	=> TXFIFO_CT3,
TXFIFO_CT4 	=> TXFIFO_CT4,
TXFIFO_CT5 	=> TXFIFO_CT5,
TXFIFO_CT6 	=> TXFIFO_CT6,
TXFIFO_CT7 	=> TXFIFO_CT7
);
--------------------------------------------------------------

INST0_SL_Receiver_x8: SL_Receiver_x8
generic map (
BV_MIN_ECOUNT => BV_MIN_ECOUNT,
BV_RX_ALMOST_FULL_CT => BV_RX_ALMOST_FULL_CT)
PORT MAP( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
PBEN_I  => PBEN_I,
SCLK 	=> SCLK,
ENA_RX	=> ENA_RX,
SL_RX	=> SL_RX,
STAT_RO => STAT_RO,
STAT_RE => STAT_RE,
INT_RS => INT_RS,
READ_PULSE => READ_PULSE,
CLR_RX_FIFO => CLR_RX_FIFO,
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
--------------------------------------------------------------



--------------------------------------------------------------
-- LINE STATUS DETECTORS
--------------------------------------------------------------

--------------------------------------------------------------
INST_SL_Line_Status_x8: SL_Line_Status_x8
generic map (BV_IDLE_COUNT => BV_IDLE_COUNT)
PORT MAP( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
PBEN_I  => PBEN_I,
SCLK 	=> SCLK,
SL_RX	=> SL_RX,
INT_LI => INT_LI
);
--------------------------------------------------------------

end rtl;