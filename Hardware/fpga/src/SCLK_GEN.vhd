----------------------------------------------------------------------------
--
--  File:   SCLK_GEN.vhd
--  Rev:    1.0.0
--  Date:	2-3-04
--  This is the VHDL module for the SLINK Clock Generator for the SLINK Peripheral Module
-- in the Streetfire RBX Companion Chip (FPGA) for Streetfire Street Racer CPU Card to 
--  Application Board Interface
--  Author: Robyn E. Bauer
--
--	History: 
--	
--	Created 2-3-04
----------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;

library unisim;
use unisim.ALL;

entity SCLK_GEN is 
	port ( 
	RST_I	:in std_logic; -- master reset for peripheral bus
	CLK_I	:in std_logic; -- master clock for peripheral bus (100MHz)
	SCLK	:out std_logic -- SLINK CLock (timebase of 10us)
	);
end SCLK_GEN;


architecture rtl of SCLK_GEN is

--  SCLK is CLK_I/500
-- SCLK:   100000Hz (10us)
-- CLK_I: 50000000Hz (0.02us)
-- Make SCLK high for 250 counts, and low for 250 counts
constant BV_HALFCYCLE_COUNT: bit_vector (7 downto 0) := x"FA"; -- 250 dec = x"FA" 
--constant BV_HALFCYCLE_COUNT: bit_vector (7 downto 0) := x"0A"; -- 32 dec = x"20" 
 

-- SIGNAL DEFINITIONS:

signal CLK_CTR: std_logic_vector(7 downto 0); -- Counter
signal HCOUNT : std_logic_vector (7 downto 0); -- std logic version OF BV_HALFCYCLE_COUNT
signal SCLK_out: std_logic;  -- intermediate signal for output


--**********************************************************
--  LOGIC BEGINS HERE:
--**********************************************************
  
begin

HCOUNT <= to_stdlogicvector(BV_HALFCYCLE_COUNT);

SCLK <= SCLK_out; 

--**********************************************************
-- CLOCK COUNTER:
--**********************************************************
CLK_COUNTER: process (CLK_I, RST_I,HCOUNT,CLK_CTR,SCLK_out)
begin

if (RST_I = '1') then	
				CLK_CTR <= (others => '0');
				SCLK_out <= '0';
elsif (CLK_I'event and CLK_I = '1') then 
	if CLK_CTR = HCOUNT then 
			CLK_CTR <= (others => '0');
			SCLK_out	<= not SCLK_out;
	else		CLK_CTR <= CLK_CTR + 1 ;
			SCLK_out 	<= SCLK_out;
	end if;
end if;
end process CLK_COUNTER;
----------------------------------------------------------------

end rtl;

