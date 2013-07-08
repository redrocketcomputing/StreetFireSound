----------------------------------------------------------------------------
--
--  File:   SL_INT_CTRL.vhd
--  Rev:    1.0.0
--  Date:	2-27-04
--  This is the VHDL module for the 40-INterrupt SLINK Interrupt Controller for 
--  the SLINK Peripheral Module in the Streetfire RBX Companion Chip (FPGA) for 
--  Streetfire Street Racer CPU Card to Application Board Interface
--
--  Author: Robyn E. Bauer
--
--
--	History: 
--	Created 2-27-04 MODIFIED FROM SL_INT_x48.vhd
----------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;

library unisim;
use unisim.ALL;

entity SL_INT_CTRL is 
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
end SL_INT_CTRL;


architecture rtl of SL_INT_CTRL is


-- SIGNAL DEFINITIONS:

signal LINT_K: std_logic_vector(31 downto 0); -- 32 INTERRUPTS

-- Components

--------------------------------------------------------------
component SL_INT_k 
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
end component; --SL_INT_k



--------------------------------------------------------------

--**********************************************************
--  LOGIC BEGINS HERE:
--**********************************************************
  
begin



-- Port Maps

-- Interrupt Assignments:

--&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
--	INT_TS(7:0)	-- Interrupt bits (31:24)
--	CLR_TS(7:0)
--	MASK_TS(7:0)
--	STAT_TS(7:0)
--------------------------------------------------------------
INST31_SL_INT_k: SL_INT_k
PORT MAP( 	RST_I => RST_I,
		CLK_I => CLK_I,
		PBEN_I => PBEN_I,
		INT_K => INT_TS(7),
		CLR_K => CLR_TS(7),
		MASK_K => MASK_TS(7),
		STAT_K => STAT_TS(7),
		LINT_K => LINT_K(31) );
--------------------------------------------------------------
INST30_SL_INT_k: SL_INT_k
PORT MAP( 	RST_I => RST_I,
		CLK_I => CLK_I,
		PBEN_I => PBEN_I,
		INT_K => INT_TS(6),
		CLR_K => CLR_TS(6),
		MASK_K => MASK_TS(6),
		STAT_K => STAT_TS(6),
		LINT_K => LINT_K(30) );
--------------------------------------------------------------
INST29_SL_INT_k: SL_INT_k
PORT MAP( 	RST_I => RST_I,
		CLK_I => CLK_I,
		PBEN_I => PBEN_I,
		INT_K => INT_TS(5),
		CLR_K => CLR_TS(5),
		MASK_K => MASK_TS(5),
		STAT_K => STAT_TS(5),
		LINT_K => LINT_K(29) );
--------------------------------------------------------------
INST28_SL_INT_k: SL_INT_k
PORT MAP( 	RST_I => RST_I,
		CLK_I => CLK_I,
		PBEN_I => PBEN_I,
		INT_K => INT_TS(4),
		CLR_K => CLR_TS(4),
		MASK_K => MASK_TS(4),
		STAT_K => STAT_TS(4),
		LINT_K => LINT_K(28) );
--------------------------------------------------------------
INST27_SL_INT_k: SL_INT_k
PORT MAP( 	RST_I => RST_I,
		CLK_I => CLK_I,
		PBEN_I => PBEN_I,
		INT_K => INT_TS(3),
		CLR_K => CLR_TS(3),
		MASK_K => MASK_TS(3),
		STAT_K => STAT_TS(3),
		LINT_K => LINT_K(27) );
--------------------------------------------------------------
INST26_SL_INT_k: SL_INT_k
PORT MAP( 	RST_I => RST_I,
		CLK_I => CLK_I,
		PBEN_I => PBEN_I,
		INT_K => INT_TS(2),
		CLR_K => CLR_TS(2),
		MASK_K => MASK_TS(2),
		STAT_K => STAT_TS(2),
		LINT_K => LINT_K(26) );
--------------------------------------------------------------
INST25_SL_INT_k: SL_INT_k
PORT MAP( 	RST_I => RST_I,
		CLK_I => CLK_I,
		PBEN_I => PBEN_I,
		INT_K => INT_TS(1),
		CLR_K => CLR_TS(1),
		MASK_K => MASK_TS(1),
		STAT_K => STAT_TS(1),
		LINT_K => LINT_K(25) );
--------------------------------------------------------------
INST24_SL_INT_k: SL_INT_k
PORT MAP( 	RST_I => RST_I,
		CLK_I => CLK_I,
		PBEN_I => PBEN_I,
		INT_K => INT_TS(0),
		CLR_K => CLR_TS(0),
		MASK_K => MASK_TS(0),
		STAT_K => STAT_TS(0),
		LINT_K => LINT_K(24) );
--------------------------------------------------------------

--&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
--	INT_RS(7:0)	-- Interrupt bits (23:16)
--	CLR_RS(7:0)
--	MASK_RS(7:0)
--	STAT_RS(7:0)
--------------------------------------------------------------
INST23_SL_INT_k: SL_INT_k
PORT MAP( 	RST_I => RST_I,
		CLK_I => CLK_I,
		PBEN_I => PBEN_I,
		INT_K => INT_RS(7),
		CLR_K => CLR_RS(7),
		MASK_K => MASK_RS(7),
		STAT_K => STAT_RS(7),
		LINT_K => LINT_K(23) );
--------------------------------------------------------------
INST22_SL_INT_k: SL_INT_k
PORT MAP( 	RST_I => RST_I,
		CLK_I => CLK_I,
		PBEN_I => PBEN_I,
		INT_K => INT_RS(6),
		CLR_K => CLR_RS(6),
		MASK_K => MASK_RS(6),
		STAT_K => STAT_RS(6),
		LINT_K => LINT_K(22) );
--------------------------------------------------------------
INST21_SL_INT_k: SL_INT_k
PORT MAP( 	RST_I => RST_I,
		CLK_I => CLK_I,
		PBEN_I => PBEN_I,
		INT_K => INT_RS(5),
		CLR_K => CLR_RS(5),
		MASK_K => MASK_RS(5),
		STAT_K => STAT_RS(5),
		LINT_K => LINT_K(21) );
--------------------------------------------------------------
INST20_SL_INT_k: SL_INT_k
PORT MAP( 	RST_I => RST_I,
		CLK_I => CLK_I,
		PBEN_I => PBEN_I,
		INT_K => INT_RS(4),
		CLR_K => CLR_RS(4),
		MASK_K => MASK_RS(4),
		STAT_K => STAT_RS(4),
		LINT_K => LINT_K(20) );
--------------------------------------------------------------
INST19_SL_INT_k: SL_INT_k
PORT MAP( 	RST_I => RST_I,
		CLK_I => CLK_I,
		PBEN_I => PBEN_I,
		INT_K => INT_RS(3),
		CLR_K => CLR_RS(3),
		MASK_K => MASK_RS(3),
		STAT_K => STAT_RS(3),
		LINT_K => LINT_K(19) );
--------------------------------------------------------------
INST18_SL_INT_k: SL_INT_k
PORT MAP( 	RST_I => RST_I,
		CLK_I => CLK_I,
		PBEN_I => PBEN_I,
		INT_K => INT_RS(2),
		CLR_K => CLR_RS(2),
		MASK_K => MASK_RS(2),
		STAT_K => STAT_RS(2),
		LINT_K => LINT_K(18) );
--------------------------------------------------------------
INST17_SL_INT_k: SL_INT_k
PORT MAP( 	RST_I => RST_I,
		CLK_I => CLK_I,
		PBEN_I => PBEN_I,
		INT_K => INT_RS(1),
		CLR_K => CLR_RS(1),
		MASK_K => MASK_RS(1),
		STAT_K => STAT_RS(1),
		LINT_K => LINT_K(17) );
--------------------------------------------------------------
INST16_SL_INT_k: SL_INT_k
PORT MAP( 	RST_I => RST_I,
		CLK_I => CLK_I,
		PBEN_I => PBEN_I,
		INT_K => INT_RS(0),
		CLR_K => CLR_RS(0),
		MASK_K => MASK_RS(0),
		STAT_K => STAT_RS(0),
		LINT_K => LINT_K(16) );
--------------------------------------------------------------

--&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
--	INT_TC(7:0)	-- Interrupt bits (15:8)
--	CLR_TC(7:0)
--	MASK_TC(7:0)
--	STAT_TC(7:0)
--------------------------------------------------------------
INST15_SL_INT_k: SL_INT_k
PORT MAP( 	RST_I => RST_I,
		CLK_I => CLK_I,
		PBEN_I => PBEN_I,
		INT_K => INT_TC(7),
		CLR_K => CLR_TC(7),
		MASK_K => MASK_TC(7),
		STAT_K => STAT_TC(7),
		LINT_K => LINT_K(15) );
--------------------------------------------------------------
INST14_SL_INT_k: SL_INT_k
PORT MAP( 	RST_I => RST_I,
		CLK_I => CLK_I,
		PBEN_I => PBEN_I,
		INT_K => INT_TC(6),
		CLR_K => CLR_TC(6),
		MASK_K => MASK_TC(6),
		STAT_K => STAT_TC(6),
		LINT_K => LINT_K(14) );
--------------------------------------------------------------
INST13_SL_INT_k: SL_INT_k
PORT MAP( 	RST_I => RST_I,
		CLK_I => CLK_I,
		PBEN_I => PBEN_I,
		INT_K => INT_TC(5),
		CLR_K => CLR_TC(5),
		MASK_K => MASK_TC(5),
		STAT_K => STAT_TC(5),
		LINT_K => LINT_K(13) );
--------------------------------------------------------------
INST12_SL_INT_k: SL_INT_k
PORT MAP( 	RST_I => RST_I,
		CLK_I => CLK_I,
		PBEN_I => PBEN_I,
		INT_K => INT_TC(4),
		CLR_K => CLR_TC(4),
		MASK_K => MASK_TC(4),
		STAT_K => STAT_TC(4),
		LINT_K => LINT_K(12) );
--------------------------------------------------------------
INST11_SL_INT_k: SL_INT_k
PORT MAP( 	RST_I => RST_I,
		CLK_I => CLK_I,
		PBEN_I => PBEN_I,
		INT_K => INT_TC(3),
		CLR_K => CLR_TC(3),
		MASK_K => MASK_TC(3),
		STAT_K => STAT_TC(3),
		LINT_K => LINT_K(11) );
--------------------------------------------------------------
INST10_SL_INT_k: SL_INT_k
PORT MAP( 	RST_I => RST_I,
		CLK_I => CLK_I,
		PBEN_I => PBEN_I,
		INT_K => INT_TC(2),
		CLR_K => CLR_TC(2),
		MASK_K => MASK_TC(2),
		STAT_K => STAT_TC(2),
		LINT_K => LINT_K(10) );
--------------------------------------------------------------
INST9_SL_INT_k: SL_INT_k
PORT MAP( 	RST_I => RST_I,
		CLK_I => CLK_I,
		PBEN_I => PBEN_I,
		INT_K => INT_TC(1),
		CLR_K => CLR_TC(1),
		MASK_K => MASK_TC(1),
		STAT_K => STAT_TC(1),
		LINT_K => LINT_K(9) );
--------------------------------------------------------------
INST8_SL_INT_k: SL_INT_k
PORT MAP( 	RST_I => RST_I,
		CLK_I => CLK_I,
		PBEN_I => PBEN_I,
		INT_K => INT_TC(0),
		CLR_K => CLR_TC(0),
		MASK_K => MASK_TC(0),
		STAT_K => STAT_TC(0),
		LINT_K => LINT_K(8) );
--------------------------------------------------------------

--&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
--	INT_LI(7:0)	-- Interrupt bits (7:0)
--	CLR_LI(7:0)
--	MASK_LI(7:0)
--	STAT_LI(7:0)
--------------------------------------------------------------
INST7_SL_INT_k: SL_INT_k
PORT MAP( 	RST_I => RST_I,
		CLK_I => CLK_I,
		PBEN_I => PBEN_I,
		INT_K => INT_LI(7),
		CLR_K => CLR_LI(7),
		MASK_K => MASK_LI(7),
		STAT_K => STAT_LI(7),
		LINT_K => LINT_K(7) );
--------------------------------------------------------------
INST6_SL_INT_k: SL_INT_k
PORT MAP( 	RST_I => RST_I,
		CLK_I => CLK_I,
		PBEN_I => PBEN_I,
		INT_K => INT_LI(6),
		CLR_K => CLR_LI(6),
		MASK_K => MASK_LI(6),
		STAT_K => STAT_LI(6),
		LINT_K => LINT_K(6) );
--------------------------------------------------------------
INST5_SL_INT_k: SL_INT_k
PORT MAP( 	RST_I => RST_I,
		CLK_I => CLK_I,
		PBEN_I => PBEN_I,
		INT_K => INT_LI(5),
		CLR_K => CLR_LI(5),
		MASK_K => MASK_LI(5),
		STAT_K => STAT_LI(5),
		LINT_K => LINT_K(5) );
--------------------------------------------------------------
INST4_SL_INT_k: SL_INT_k
PORT MAP( 	RST_I => RST_I,
		CLK_I => CLK_I,
		PBEN_I => PBEN_I,
		INT_K => INT_LI(4),
		CLR_K => CLR_LI(4),
		MASK_K => MASK_LI(4),
		STAT_K => STAT_LI(4),
		LINT_K => LINT_K(4) );
--------------------------------------------------------------
INST3_SL_INT_k: SL_INT_k
PORT MAP( 	RST_I => RST_I,
		CLK_I => CLK_I,
		PBEN_I => PBEN_I,
		INT_K => INT_LI(3),
		CLR_K => CLR_LI(3),
		MASK_K => MASK_LI(3),
		STAT_K => STAT_LI(3),
		LINT_K => LINT_K(3) );
--------------------------------------------------------------
INST2_SL_INT_k: SL_INT_k
PORT MAP( 	RST_I => RST_I,
		CLK_I => CLK_I,
		PBEN_I => PBEN_I,
		INT_K => INT_LI(2),
		CLR_K => CLR_LI(2),
		MASK_K => MASK_LI(2),
		STAT_K => STAT_LI(2),
		LINT_K => LINT_K(2) );
--------------------------------------------------------------
INST1_SL_INT_k: SL_INT_k
PORT MAP( 	RST_I => RST_I,
		CLK_I => CLK_I,
		PBEN_I => PBEN_I,
		INT_K => INT_LI(1),
		CLR_K => CLR_LI(1),
		MASK_K => MASK_LI(1),
		STAT_K => STAT_LI(1),
		LINT_K => LINT_K(1) );
--------------------------------------------------------------
INST0_SL_INT_k: SL_INT_k
PORT MAP( 	RST_I => RST_I,
		CLK_I => CLK_I,
		PBEN_I => PBEN_I,
		INT_K => INT_LI(0),
		CLR_K => CLR_LI(0),
		MASK_K => MASK_LI(0),
		STAT_K => STAT_LI(0),
		LINT_K => LINT_K(0) );
--------------------------------------------------------------







--**********************************************************
-- SL_INT  LOGIC:
--**********************************************************
SL_INT_GEN: process (CLK_I,RST_I,PBEN_I,LINT_K)
begin
if (RST_I = '1' or PBEN_I = '0') then	SL_INT  <= '0';
elsif (CLK_I'event and CLK_I = '1') then 

SL_INT <= 		
		LINT_K(31) or LINT_K(30) or LINT_K(29) or LINT_K(28) or
		LINT_K(27) or LINT_K(26) or LINT_K(25) or LINT_K(24) or
		LINT_K(23) or LINT_K(22) or LINT_K(21) or LINT_K(20) or
		LINT_K(19) or LINT_K(18) or LINT_K(17) or LINT_K(16) or
		LINT_K(15) or LINT_K(14) or LINT_K(13) or LINT_K(12) or
		LINT_K(11) or LINT_K(10) or LINT_K(9) or LINT_K(8) or
		LINT_K(7) or LINT_K(6) or LINT_K(5) or LINT_K(4) or
		LINT_K(3) or LINT_K(2) or LINT_K(1) or LINT_K(0);

end if;
end process SL_INT_GEN;
--------------------------------------------------------------

end rtl;

