----------------------------------------------------------------------------
--
--  File:   RBX_CONMOD.vhd
--  Rev:    1.0.0
--  Date:	3-6-04
--  This is the clock and reset control module for the RBX Companion Chip 
--  FPGA Code for  the Streetfire Street Racer CPU Card to Application Board Interface
--  Author: Robyn E. Bauer
--
--	History: 
--
--	3-12-04 Added global clock buffer to mclk output
--	3-6-04 Added global clock buffer to CLK_I output
--		Removed global clock buffer from 100MHz input	
--		removed global clock buffer from unused CLK_3_6MHz signal
--		Changed Main Clock output name to CLK_I, and added global buffer	
--	Created 11-25-03
----------------------------------------------------------------------------

library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;

library unisim;
use unisim.ALL;

entity RBX_CONMOD is
	port(
-- XSCALE CLOCK INPUTS:
	XS_SDCLK0	:in	std_logic;	--LOC = "B8" ; #GCK2 Synchronous Static Memory clock from CPU
	CLK_3_6MHz	:in	std_logic;	--LOC = "T9" ; #GCK0 (x-scale 3.6MHz/GPIO[11])
-- Two (2) Edge Bus Connector Clock inputs (or general purpose inputs)
	EB_GCK3_13	:in  std_logic; --LOC="C8" GCK3 from edge NOT CONNECTED on test board
	EB_GCK1_147	:in  std_logic;	--LOC="T8" GCK1 from edge (test board I2S_SCLK)
-- Reset input from XSCALE
	XS_PWM1 	:in	std_logic;	--XS GPIO17 -use as active low fpga reset input
-- OUTPUTS
	RST_O		:out	std_logic;	-- master reset active high
	CLK_I		:out 	std_logic;	-- master clock
-- EXTRA OUPTUTS (TO GUARANTEE CLOCK BUFFER PLACEMENT)
	CLK_3_6MHz_IBUFG: out std_logic;
	EB_GCK1_147_IBUFG: out std_logic;
	EB_GCK3_13_IBUFG: out std_logic
	);
end RBX_CONMOD;

architecture rtl of RBX_CONMOD is

-- COMPONENT DECLARATIONS:

component IBUFG
port (O : out STD_ULOGIC;
I : in STD_ULOGIC);
end component;

component IBUF
port (O : out STD_ULOGIC;
I : in STD_ULOGIC);
end component;

component BUFG
port (O : out STD_ULOGIC;
I : in STD_ULOGIC);
end component;

-- SIGNAL DEFINITIONS:

signal SDCLK0_IN, SM_CLK:std_logic;

signal gck3,gck1:std_logic;

--**********************************************************
--  LOGIC BEGINS HERE:
--**********************************************************
  
begin


--%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
--	CLOCKING
--%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

--	3-6-04 Added global clock buffer to CLK_I output
--CLK_O <= SM_CLK;
--CLK_I <= SM_CLK;

BUFG_CLK_I : BUFG port map (O => CLK_I,I => SM_CLK);


-- IBUFG's for clock inputs:  (denotes a clock input)

--3-6-04 removed global clock buffer from unused CLK_3_6MHz signal (reserved for tool)
--IBUFG_GCLK0 : IBUFG port map (O => CLK_3_6MHz_IBUFG, I => CLK_3_6MHz);
--IBUFG_GCLK0 : IBUF port map (O => CLK_3_6MHz_IBUFG, I => CLK_3_6MHz);
CLK_3_6MHz_IBUFG <= CLK_3_6MHz;

--3-6-04 Removed global clock buffer from 100MHz input (to use for CLK_I)
IBUFG_GCLK2 : IBUFG port map (O => SDCLK0_IN, I => XS_SDCLK0);
--IBUFG_GCLK2 : IBUF port map (O => SDCLK0_IN, I => XS_SDCLK0);
-- SDCLK0_IN <= XS_SDCLK0;


--3-12-04 added bufg to gck's
--IBUFG_GCLK1 : IBUFG port map (O => gck1, I => EB_GCK1_147);
IBUFG_GCLK3 : IBUFG port map (O => gck3, I => EB_GCK3_13);
--BUFG_GCLK1: BUFG port map (O => EB_GCK1_147_IBUFG,I => gck1);
BUFG_GCLK3: BUFG port map (O => EB_GCK3_13_IBUFG,I => gck3);

-- previous code:
IBUFG_GCLK1 : IBUFG port map (O => EB_GCK1_147_IBUFG, I => EB_GCK1_147);
--IBUFG_GCLK3 : IBUFG port map (O => EB_GCK3_13_IBUFG, I => EB_GCK3_13);


--Divide processor bus clock by two to run the state machine(s)
--STATE MACHINE CLOCK GENERATION
STATE_MACH_CLK: process(SDCLK0_IN,SM_CLK,XS_PWM1) begin
if (XS_PWM1 = '0') then SM_CLK <= '0';
elsif (SDCLK0_IN 'event and SDCLK0_IN = '1') then 
		SM_CLK <= not SM_CLK; -- divide by 2
end if;
end process STATE_MACH_CLK;

RST_O <= not XS_PWM1;

end rtl;


