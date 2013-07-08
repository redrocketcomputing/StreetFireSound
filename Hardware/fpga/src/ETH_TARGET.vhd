-- -------------------------------------------------------
-- FILE: ETH_TARGET.vhd
-- Date: 1-29-04
-- Author: R. Bauer 
--
-- Purpose: Provides Edge Bus Interface response to StreetRacer FPGA--
--	This module corresponds to the E0 (LAN91C111) interface
-- Note: This module is NOT syntheziable! It if for simulation only !!!
--
-- Revision: 0.0	
--
-- History: 
--
--		Converted ETH_TARGET from E0_TARGET for APP Board Version, 1-29-04, REB
--		0.0 -> initial draft 10-02-03
--	
--
-- -------------------------------------------------------
LIBRARY ieee;
USE ieee.std_logic_1164.ALL;
USE ieee.numeric_std.ALL;
USE std.textio.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
library unisim;
use unisim.Vcomponents.all;

entity ETH_TARGET is
    Port ( 
	SIM_CLK: in std_logic;
	DATA	: inout std_logic_vector (31 downto 0); 
	ADDR	: in	std_logic_vector (3 downto 1); 
			
	E0_CS	: in   std_logic; 
	E0_ADS  : in   std_logic;
	E0_RD	: in	std_logic;
	E0_WR	: in	std_logic;
	E1_RESET: in 	std_logic
	);    								
end ETH_TARGET;

architecture Behavioral of ETH_TARGET is



-- Components
component sram_target
	port (
	addr: IN std_logic_VECTOR(13 downto 0);
	clk: IN std_logic;
	din: IN std_logic_VECTOR(31 downto 0);
	dout: OUT std_logic_VECTOR(31 downto 0);
	sinit: IN std_logic;
	we: IN std_logic);
end component;

-- XST black box declaration
attribute box_type : string;
attribute box_type of sram_target: component is "black_box";

-- FPGA Express Black Box declaration
attribute fpga_dont_touch: string;
attribute fpga_dont_touch of sram_target: component is "true";

-- Synplicity black box declaration
attribute syn_black_box : boolean;
attribute syn_black_box of sram_target: component is true;

signal D_OUT: std_logic_vector(31 downto 0);


begin

--Data output tristate:

E0_DATA_TRISTATE_OUT: process (E0_CS,E0_RD,D_OUT)
begin
if (E0_CS = '0' AND E0_RD = '0') then DATA <= D_OUT;
else DATA <= (others => 'Z');
end if; 
end process E0_DATA_TRISTATE_OUT;

E0_BRAM1 : sram_target	port map (
			addr(13 downto 3) => "00000000000",
			addr(2 downto 0) => ADDR(3 downto 1),   
			clk => SIM_CLK,
			din => DATA,
			dout => D_OUT,
			sinit => E1_RESET,
			we => E0_WR );

end Behavioral;

		     

