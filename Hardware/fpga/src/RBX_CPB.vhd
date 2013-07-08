----------------------------------------------------------------------------
--
--  File:   RBX_CPB.vhd
--  Rev:    1.0.0
--  Date:	3-5-03
--  This is the VHDL module for the MASTER interface for the RBX Companion Chip
--	(FPGA) for Streetfire Street Racer CPU Card to Application Board Interface
--  Author: Robyn E. Bauer
--
--	History: 
--	
--	Sync'd Intermodule Inputs WE_I,STB_I, 3-5-04	
--	Created 11-25-03
----------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;

library unisim;
use unisim.ALL;

entity RBX_CPB is 
	generic(VERSION_NUMBER,BLOCK_CAPABILITY: bit_vector (31 downto 0));
	port ( 

-- SYSTEM CONTROL INPUTS
	RST_I		:in std_logic; -- master reset for peripheral bus
	CLK_I		:in std_logic; -- master clock for peripheral bus

-- PERIPHERAL BUS SIGNALS
	WE_I		:in std_logic; -- Write/read indicator for strobe (1 = write)
	CYC_I		:in std_logic; -- cycle indicator
	ADR_I		:in std_logic_vector(17 downto 0); -- peripheral address
	SEL_I		:in std_logic_vector(3 downto 0);  -- peripheral byte selects (active high)
	DAT_I		:in std_logic_vector(31 downto 0); -- peripheral data in to slave 
	STB_I		:in std_logic; -- write/read strobe (active high)
	DAT_O		:out std_logic_vector(31 downto 0);  -- peripheral data out from slave
	ACK_O		:out std_logic; -- peripheral ack (active high)
	
	PB_EN		:out std_logic_vector(31 downto 0)  -- peripheral block enable signals (active high)

	);
end RBX_CPB;


architecture rtl of RBX_CPB is

-- REGISTER ADDRESSES: 
constant VER_REG_ADDR: 	 bit_vector (17 downto 0) := "000000000000000000"; -- 0x0
constant PBCAP_REG_ADDR: bit_vector (17 downto 0) := "000000000000000100"; -- 0x4
constant PBEN_REG_ADDR:  bit_vector (17 downto 0) := "000000000000001000"; -- 0x8


-- SIGNAL DEFINITIONS:


-- Registered signals


-- Peripheral Slave State Machine

type SLAVE_StateType is (
SLAVE_IDLE, 
WRITE_START,
WRITE_STROBE,
WRITE_ACK,
READ_START,
READ_STROBE,
READ_ACK);

signal SLAVE_currentState, SLAVE_nextState : SLAVE_StateType;

signal VER_REG,PBCAP_REG,PBEN_REG : std_logic_vector(31 downto 0); -- registers

signal ADR_I_BV:bit_vector(17 downto 0); -- bit vector version of adr_i for case

signal WE_I_IN,STB_I_IN:std_logic; --3-5-04 Added intermodule sync for WE_I,STB_I


--**********************************************************
--  LOGIC BEGINS HERE:
--**********************************************************
  
begin

-------------------------------------------------
--3-5-04 Added intermodule sync for WE_I,STB_I
-------------------------------------------------
WE_I_IN_REG: process (CLK_I,WE_I) 
begin 
if (CLK_I 'event and CLK_I = '1') then WE_I_IN <= WE_I;
end if;
end process WE_I_IN_REG;
-------------------------------------------------
STB_I_IN_REG: process (CLK_I,STB_I) 
begin 
if (CLK_I 'event and CLK_I = '1') then STB_I_IN <= STB_I;
end if;
end process STB_I_IN_REG;
-------------------------------------------------




--**********************************************************
-- STATE MACHINE
--**********************************************************
--SLAVE STATE MACHINE SYNCHRONIZATION 
SLAVE_currentStateProc: process(RST_I, CLK_I, SLAVE_nextState) begin
      if (RST_I = '1') then SLAVE_currentState <= SLAVE_IDLE;
       elsif (CLK_I'event and CLK_I = '1') then 
		SLAVE_currentState <= SLAVE_nextState;
      end if;
end process SLAVE_currentStateProc;

-- SLAVE STATE MACHINE TRANSTIONS

SLAVE_nextStateProc: process(SLAVE_currentState,STB_I_IN,WE_I_IN) 
begin
case SLAVE_currentState is

when SLAVE_IDLE =>
if STB_I_IN = '1' and WE_I_IN = '1' then  SLAVE_nextState <= WRITE_START;
elsif STB_I_IN = '1' and WE_I_IN = '0' then  SLAVE_nextState <= READ_START;
end if;

when WRITE_START => SLAVE_nextState <= WRITE_STROBE;

when WRITE_STROBE => SLAVE_nextState <= WRITE_ACK;

when WRITE_ACK =>
if STB_I_IN = '0' then SLAVE_nextState <=  SLAVE_IDLE;
end if;

when READ_START => SLAVE_nextState <= READ_STROBE;

when READ_STROBE => SLAVE_nextState <= READ_ACK;

when READ_ACK => 
if STB_I_IN = '0' then SLAVE_nextState <=  SLAVE_IDLE;
end if;

when others => SLAVE_nextState <=  SLAVE_IDLE;

end case;
end process SLAVE_nextStateProc;

--**********************************************************
-- END STATE MACHINE
--**********************************************************

ADR_I_BV <= to_bitvector(ADR_I);

-- ACK_O output: 

ACK_O_GEN: process (SLAVE_currentState)
begin
case SLAVE_currentState is
when WRITE_ACK 	=> ACK_O <= '1';
when READ_ACK  	=> ACK_O <= '1';
when others 	=> ACK_O <= '0';
end case;
end process ACK_O_GEN;

-- DAT_O output:
-- DATA OUTPUT MUX:
DAT_O_GEN: process (ADR_I_BV,VER_REG,PBCAP_REG,PBEN_REG)
begin
case ADR_I_BV is
when VER_REG_ADDR => DAT_O <= VER_REG;
when PBCAP_REG_ADDR => DAT_O <= PBCAP_REG;
when PBEN_REG_ADDR => DAT_O <= PBEN_REG;
when others => DAT_O <= VER_REG;

end case;

end process DAT_O_GEN;


-- REGISTERS
--constant VER_REG_ADDR:   bit_vector (17 downto 0) := "000000000000000000"; -- 0x0
--constant PBCAP_REG_ADDR: bit_vector (17 downto 0) := "000000000000000100"; -- 0x4
--constant PBEN_REG_ADDR:  bit_vector (17 downto 0) := "000000000000001000"; -- 0x8

-- VERSION REGISTER: (Read only)
VER_REG <= to_stdlogicvector(VERSION_NUMBER); -- read only version number passed as generic from top level

-- PERIPHERAL BLOCK CAPABILITY REGISTER: (Read only)
PBCAP_REG <= to_stdlogicvector(BLOCK_CAPABILITY);

-- PERIPHERAL BLOCK ENABLE REGISTER (READ/WRITE)
PBEN_REG_GEN: process (RST_I, CLK_I,SEL_I,SLAVE_currentState, ADR_I_BV, DAT_I)
begin 
if (RST_I = '1') then	PBEN_REG  <= (others => '0');
elsif (CLK_I'event and CLK_I = '1') then 
	if (SLAVE_currentState = WRITE_STROBE) and (ADR_I_BV = PBEN_REG_ADDR) then
		if SEL_I(3) = '1' then PBEN_REG(31 downto 24) <= DAT_I(31 downto 24);
		end if;
		if SEL_I(2) = '1' then PBEN_REG(23 downto 16) <= DAT_I(23 downto 16);
		end if;
		if SEL_I(1) = '1' then PBEN_REG(15 downto 8) <= DAT_I(15 downto 8);
		end if;
		if SEL_I(0) = '1' then PBEN_REG(7 downto 0) <= DAT_I(7 downto 0);
		end if;
	end if;
end if;
end process PBEN_REG_GEN;

PB_EN <= PBEN_REG;  -- assign outputs to register values





end rtl;

