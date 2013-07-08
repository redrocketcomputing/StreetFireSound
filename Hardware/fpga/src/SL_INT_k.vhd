----------------------------------------------------------------------------
--
--  File:   SL_INT_k.vhd
--  Rev:    1.0.0
--  Date:	2-27-04
--  This is the VHDL module for the Kth SLINK Interrupt Element for the SLINK Peripheral Module
-- in the Streetfire RBX Companion Chip (FPGA) for Streetfire Street Racer CPU Card to 
--  Application Board Interface
--
--  Author: Robyn E. Bauer
--
--
--	History: 
--	
--	Created 2-6-04
----------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;

library unisim;
use unisim.ALL;

entity SL_INT_k is
	port ( 
	RST_I	:in std_logic; -- master reset for peripheral bus
	CLK_I	:in std_logic; -- master clock for peripheral bus
	PBEN_I	:in std_logic; -- active high peripheral block enable 
	INT_K	:in std_logic; -- Interrupt input
	CLR_K	:in std_logic; -- Interrupt Clear (active high)
	MASK_K	:in std_logic; -- Interrupt Mask  (active low: 1 = unmasked)
	STAT_K	:out std_logic; -- Interrupt LAtch Status
	LINT_K  :out std_logic -- Interrupt qualified with mask bit
	);
end SL_INT_k;


architecture rtl of SL_INT_k is


-- SIGNAL DEFINITIONS:

signal INT_K_D1,INT_K_D2: std_logic; -- sync'd and delayed INT_K for edge detect
signal CLR_K_D1,CLR_K_D2: std_logic; -- sync'd and delayed CLR_K for edge detect
signal STAT_K_out: std_logic;  -- output also required for other logic

--**********************************************************
--  LOGIC BEGINS HERE:
--**********************************************************
  
begin

--**********************************************************
-- LINT_K  LOGIC:
--**********************************************************
LINT_K_GEN: process (CLK_I,RST_I,PBEN_I, MASK_K,STAT_K_out)
begin
if (RST_I = '1' or PBEN_I = '0') then	LINT_K  <= '0';
elsif (CLK_I'event and CLK_I = '1') then LINT_K <= MASK_K and STAT_K_out;
end if;
end process LINT_K_GEN;

----------------------------------------------------------------

--**********************************************************
-- STAT_K  LOGIC:
--**********************************************************
STAT_K <= STAT_K_out;

-- Set on pos edge of INT_K, clear on pos edge of CLR_K
STAT_K_GEN: process (CLK_I,RST_I,PBEN_I, INT_K_D1,INT_K_D2,CLR_K_D1,CLR_K_D2)
begin
if (RST_I = '1' or PBEN_I = '0') then	STAT_K_out  <= '0';
elsif (CLK_I'event and CLK_I = '1') then 
	if CLR_K_D1 = '1' and CLR_K_D2 = '0' then  STAT_K_out  <= '0';
	elsif  INT_K_D1 = '1' and INT_K_D2 = '0' then	STAT_K_out  <= '1';
	end if;
end if;
end process STAT_K_GEN;
----------------------------------------------------------------

--**********************************************************
-- INT_K Synch/Delay LOGIC:
--**********************************************************
INT_K_SYNCH: process (CLK_I, RST_I, PBEN_I,INT_K,INT_K_D1)
begin
if (RST_I = '1' or PBEN_I = '0' ) then 
			INT_K_D1 <= '0';
			INT_K_D2 <= '0';
elsif (CLK_I'event and CLK_I = '1') then 
			INT_K_D1 <= INT_K;
			INT_K_D2 <= INT_K_D1;
end if;
end process INT_K_SYNCH;
----------------------------------------------------------------

--**********************************************************
-- CLR_K Synch/Delay LOGIC:
--**********************************************************
CLR_K_SYNCH: process (CLK_I, RST_I, PBEN_I, CLR_K,CLR_K_D1)
begin
if (RST_I = '1' or PBEN_I = '0' ) then 
			CLR_K_D1 <= '0';
			CLR_K_D2 <= '0';
elsif (CLK_I'event and CLK_I = '1') then 
			CLR_K_D1 <= CLR_K;
			CLR_K_D2 <= CLR_K_D1;
end if;
end process CLR_K_SYNCH;
----------------------------------------------------------------

end rtl;

