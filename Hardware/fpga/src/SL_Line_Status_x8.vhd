----------------------------------------------------------------------------
--
--  File:   SL_Line_Status_x8.vhd
--  Rev:    1.0.0
--  Date:	2-27-04
--  This is the VHDL module for the Kth SLINK Line Status Module for the SLINK Peripheral Module
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

entity SL_Line_Status_x8 is 
	generic(BV_IDLE_COUNT: bit_vector (7 downto 0)  ); --Idle count in CLK_I's
	port ( 
	RST_I	:in std_logic; -- master reset for peripheral bus
	CLK_I	:in std_logic; -- master clock for peripheral bus
	PBEN_I  : in std_logic;
	SCLK	:in std_logic; -- SLINK CLock (timebase of 10us)
	SL_RX	: in std_logic_vector(7 downto 0); -- SLINK RX signal
	INT_LI	:out std_logic_vector(7 downto 0)  -- INDICATES LINE IS IDLE (AFTER 3 DELIMITER TIMES)
);
end SL_Line_Status_x8;


architecture rtl of SL_Line_Status_x8 is


-- SIGNAL DEFINITIONS:

component SL_Line_Status 
	generic(BV_IDLE_COUNT: bit_vector (7 downto 0)  ); --Idle count in CLK_I's
	port ( 
	RST_I	:in std_logic; -- master reset for peripheral bus
	CLK_I	:in std_logic; -- master clock for peripheral bus
	PBEN_I  : in std_logic;
	SCLK	:in std_logic; -- SLINK CLock (timebase of 10us)
	SL_RX	: in std_logic; -- SLINK RX signal
	INT_LI 	:out std_logic  -- INDICATES LINE IS IDLE (AFTER 3 DELIMITER TIMES)
);
end component; --SL_Line_Status



--**********************************************************
--  LOGIC BEGINS HERE:
--**********************************************************
  
begin

--------------------------------------------------------------
INST0_SL_Line_Status: SL_Line_Status 
generic map (BV_IDLE_COUNT => BV_IDLE_COUNT)
PORT MAP( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
PBEN_I  => PBEN_I,
SCLK 	=> SCLK,
SL_RX	=> SL_RX(0),
INT_LI => INT_LI(0)
);
--------------------------------------------------------------
INST1_SL_Line_Status: SL_Line_Status 
generic map (BV_IDLE_COUNT => BV_IDLE_COUNT)
PORT MAP( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
PBEN_I  => PBEN_I,
SCLK 	=> SCLK,
SL_RX	=> SL_RX(1),
INT_LI => INT_LI(1)
);

--------------------------------------------------------------
INST2_SL_Line_Status: SL_Line_Status 
generic map (BV_IDLE_COUNT => BV_IDLE_COUNT)
PORT MAP( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
PBEN_I  => PBEN_I,
SCLK 	=> SCLK,
SL_RX	=> SL_RX(2),
INT_LI => INT_LI(2)
);

--------------------------------------------------------------
INST3_SL_Line_Status: SL_Line_Status 
generic map (BV_IDLE_COUNT => BV_IDLE_COUNT)
PORT MAP( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
PBEN_I  => PBEN_I,
SCLK 	=> SCLK,
SL_RX	=> SL_RX(3),
INT_LI => INT_LI(3)
);

--------------------------------------------------------------
INST4_SL_Line_Status: SL_Line_Status 
generic map (BV_IDLE_COUNT => BV_IDLE_COUNT)
PORT MAP( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
PBEN_I  => PBEN_I,
SCLK 	=> SCLK,
SL_RX	=> SL_RX(4),
INT_LI => INT_LI(4)
);

--------------------------------------------------------------
INST5_SL_Line_Status: SL_Line_Status 
generic map (BV_IDLE_COUNT => BV_IDLE_COUNT)
PORT MAP( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
PBEN_I  => PBEN_I,
SCLK 	=> SCLK,
SL_RX	=> SL_RX(5),
INT_LI => INT_LI(5)
);

--------------------------------------------------------------
INST6_SL_Line_Status: SL_Line_Status 
generic map (BV_IDLE_COUNT => BV_IDLE_COUNT)
PORT MAP( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
PBEN_I  => PBEN_I,
SCLK 	=> SCLK,
SL_RX	=> SL_RX(6),
INT_LI => INT_LI(6)
);

--------------------------------------------------------------
INST7_SL_Line_Status: SL_Line_Status 
generic map (BV_IDLE_COUNT => BV_IDLE_COUNT)
PORT MAP( 
RST_I	=> RST_I,
CLK_I	=> CLK_I,
PBEN_I  => PBEN_I,
SCLK 	=> SCLK,
SL_RX	=> SL_RX(7),
INT_LI => INT_LI(7)
);


end rtl;

