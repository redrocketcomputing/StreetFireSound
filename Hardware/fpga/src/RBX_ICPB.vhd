----------------------------------------------------------------------------
--
--  File:   RBX_ICPB.vhd
--  Rev:    1.0.0
--  Date:	3-5-04
--  This is the VHDL module for the MASTER interface for the RBX Companion Chip
--	(FPGA) for Streetfire Street Racer CPU Card to Application Board Interface
--  Author: Robyn E. Bauer
--
--	History: 
--
--	Sync'd Intermodule Inputs WE_I,STB_I, 3-5-04
--	Increased Clear Pulse to include ACK state, 3-5-04, REB
--	Added explicit edge detection circuit for Status Register, 1-12-04, REB	
--	Created 12-23-03
----------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;

library unisim;
use unisim.ALL;

entity RBX_ICPB is 
	port ( 

-- SYSTEM CONTROL INPUTS
	RST_I	:in std_logic; -- master reset for peripheral bus
	CLK_I	:in std_logic; -- master clock for peripheral bus

-- PERIPHERAL BUS SIGNALS
	WE_I	:in std_logic; -- Write/read indicator for strobe (1 = write)
	CYC_I	:in std_logic; -- cycle indicator
	ADR_I	:in std_logic_vector(17 downto 0); -- peripheral address
	SEL_I	:in std_logic_vector(3 downto 0);  -- peripheral byte selects (active high)
	DAT_I	:in std_logic_vector(31 downto 0); -- peripheral data in to slave 
	STB_I	:in std_logic; -- write/read strobe (active high)
	DAT_O	:out std_logic_vector(31 downto 0);  -- peripheral data out from slave
	ACK_O	:out std_logic; -- peripheral ack (active high)

-- MODULE SPECIFIC SIGNALS
	PBEN_I	:in std_logic; -- module enable (active high)
	PB_INT	:in std_logic_vector(31 downto 0);  --  peripheral interrupt inputs (pos edge)
	PBINT_O	:out std_logic  -- interrupt output(active high)

	);
end RBX_ICPB;


architecture rtl of RBX_ICPB is

-- REGISTER ADDRESSES:
-- Interrupt Status Register: 0x00  (Read Only)
-- Interrupt Mask Register: 0x04  (R/W)
-- Interrupt Enable Register: 0x08  (R/W)
-- Interrupt Clear Register: 0x0C  (Write Only)
-- Interrupt Pending Register: 0x10 (Read Only)
--  
constant INT_STAT_REG_ADDR: bit_vector (17 downto 0) := "000000000000000000"; -- 0x0
constant INT_MASK_REG_ADDR: bit_vector (17 downto 0) := "000000000000000100"; -- 0x4
constant INT_ENA_REG_ADDR:  bit_vector (17 downto 0) := "000000000000001000"; -- 0x8
constant INT_CLR_REG_ADDR:  bit_vector (17 downto 0) := "000000000000001100"; -- 0xC
constant INT_PEN_REG_ADDR:  bit_vector (17 downto 0) := "000000000000010000"; -- 0x10


-- SIGNAL DEFINITIONS:

signal INT_STAT_REG, INT_MASK_REG, INT_ENA_REG, INT_CLR_REG, 
	INT_PEN_REG : std_logic_vector(31 downto 0); -- registers

signal MASKED_INT: std_logic_vector(31 downto 0);  -- status reg  and'ed with mask reg
signal INT_PULSE: std_logic_vector(31 downto 0);  -- delayed version of INT_PEN_REG for pulse generation

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
SLAVE_currentStateProc: process(RST_I, CLK_I, SLAVE_nextState,PBEN_I) begin
      if (RST_I = '1' or PBEN_I = '0') then SLAVE_currentState <= SLAVE_IDLE;
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
DAT_O_GEN: process (ADR_I_BV,INT_STAT_REG,INT_MASK_REG,INT_ENA_REG,INT_CLR_REG,INT_PEN_REG)
begin
case ADR_I_BV is
when	INT_STAT_REG_ADDR	=> DAT_O <= 	INT_STAT_REG	;
when	INT_MASK_REG_ADDR	=> DAT_O <= 	INT_MASK_REG	;
when	INT_ENA_REG_ADDR	=> DAT_O <= 	INT_ENA_REG	;
when	INT_CLR_REG_ADDR	=> DAT_O <= 	INT_CLR_REG	;
when	INT_PEN_REG_ADDR	=> DAT_O <= 	INT_PEN_REG	;
when	others			=> DAT_O <= 	INT_STAT_REG	;
end case;
end process DAT_O_GEN;


-- REGISTERS


-- Interrupt Pending Register: 0x10 (Read Only)

INT_PEN_REG_GEN: process (PBEN_I,RST_I, CLK_I,PB_INT)
begin 
if (RST_I = '1' or PBEN_I = '0') then	INT_PEN_REG  <= (others => '0');
elsif (CLK_I'event and CLK_I = '1') then 
	INT_PEN_REG <=	PB_INT;	
end if;
end process INT_PEN_REG_GEN;


-- Interrupt Pulse Flip Flop -- 1 clock delayed version of INT_PEN_REG
-- used for edge detection of interrupt

INT_PULSE_GEN: process(RST_I,PBEN_I,CLK_I,INT_PEN_REG)
begin
if (RST_I = '1' or PBEN_I = '0') then INT_PULSE <= (others => '0');
elsif (CLK_I'event and CLK_I = '1') then 
	INT_PULSE <= INT_PEN_REG;
end if;
end process INT_PULSE_GEN;


-- Interrupt Status Register: 0x00  (Read Only)

INT_STAT_REG_GEN: 
for I in 31 downto 0 generate 
process (CLK_I,PBEN_I,RST_I, INT_PULSE,INT_PEN_REG, INT_ENA_REG,INT_CLR_REG)
begin
if (RST_I = '1'or PBEN_I = '0') then	INT_STAT_REG(I)  <= '0';
elsif (CLK_I'event and CLK_I = '1') then 
	if INT_CLR_REG(I) = '1' then  INT_STAT_REG(I)  <= '0';
	elsif INT_ENA_REG(I) = '1' and INT_PULSE(I) = '0' and INT_PEN_REG(I) = '1' then
						INT_STAT_REG(I) <= '1';
	end if;
end if;
end process;
end generate INT_STAT_REG_GEN;

-- Interrupt Mask Register: 0x04  (R/W)


INT_MASK_REG_GEN: process (PBEN_I,RST_I,CLK_I,SEL_I,SLAVE_currentState, ADR_I_BV, DAT_I)
begin 
if (RST_I = '1'or PBEN_I = '0') then	INT_MASK_REG  <= (others => '0');
elsif (CLK_I'event and CLK_I = '1') then 
	if (SLAVE_currentState = WRITE_STROBE) and (ADR_I_BV = INT_MASK_REG_ADDR) then
		if SEL_I(3) = '1' then INT_MASK_REG(31 downto 24) <= DAT_I(31 downto 24);
		end if;
		if SEL_I(2) = '1' then INT_MASK_REG(23 downto 16) <= DAT_I(23 downto 16);
		end if;
		if SEL_I(1) = '1' then INT_MASK_REG(15 downto 8) <= DAT_I(15 downto 8);
		end if;
		if SEL_I(0) = '1' then INT_MASK_REG(7 downto 0) <= DAT_I(7 downto 0);
		end if;
	end if;
end if;
end process INT_MASK_REG_GEN;

-- Interrupt Enable Register: 0x08  (R/W)

INT_ENA_REG_GEN: process (PBEN_I,RST_I, CLK_I,SEL_I,SLAVE_currentState, ADR_I_BV, DAT_I)
begin 
if (RST_I = '1'or PBEN_I = '0') then	INT_ENA_REG  <= (others => '0');
elsif (CLK_I'event and CLK_I = '1') then 
	if (SLAVE_currentState = WRITE_STROBE) and (ADR_I_BV = INT_ENA_REG_ADDR) then
		if SEL_I(3) = '1' then INT_ENA_REG(31 downto 24) <= DAT_I(31 downto 24);
		end if;
		if SEL_I(2) = '1' then INT_ENA_REG(23 downto 16) <= DAT_I(23 downto 16);
		end if;
		if SEL_I(1) = '1' then INT_ENA_REG(15 downto 8) <= DAT_I(15 downto 8);
		end if;
		if SEL_I(0) = '1' then INT_ENA_REG(7 downto 0) <= DAT_I(7 downto 0);
		end if;
	end if;
end if;
end process INT_ENA_REG_GEN;

-- Interrupt Clear Register: 0x0C  (Write Only)

INT_CLR_REG_GEN: process (PBEN_I,RST_I, CLK_I,SEL_I,SLAVE_currentState, ADR_I_BV, DAT_I,INT_CLR_REG)
begin 
if (RST_I = '1'or PBEN_I = '0') then	INT_CLR_REG  <= (others => '0');
elsif (CLK_I'event and CLK_I = '1') then 
	if (SLAVE_currentState = WRITE_STROBE) and (ADR_I_BV = INT_CLR_REG_ADDR) then
		if SEL_I(3) = '1' then INT_CLR_REG(31 downto 24) <= DAT_I(31 downto 24);
		end if;
		if SEL_I(2) = '1' then INT_CLR_REG(23 downto 16) <= DAT_I(23 downto 16);
		end if;
		if SEL_I(1) = '1' then INT_CLR_REG(15 downto 8) <= DAT_I(15 downto 8);
		end if;
		if SEL_I(0) = '1' then INT_CLR_REG(7 downto 0) <= DAT_I(7 downto 0);
		end if;
--------------------------------------------------------------------------------------
--3-5-04 added ack state to extend clear pulse
	elsif (SLAVE_currentState = WRITE_ACK) and (ADR_I_BV = INT_CLR_REG_ADDR) then
		if SEL_I(3) = '1' then INT_CLR_REG(31 downto 24) <= INT_CLR_REG(31 downto 24);
		end if;
		if SEL_I(2) = '1' then INT_CLR_REG(23 downto 16) <= INT_CLR_REG(23 downto 16);
		end if;
		if SEL_I(1) = '1' then INT_CLR_REG(15 downto 8) <= INT_CLR_REG(15 downto 8);
		end if;
		if SEL_I(0) = '1' then INT_CLR_REG(7 downto 0) <= INT_CLR_REG(7 downto 0);
		end if;
--------------------------------------------------------------------------------------
	else 	INT_CLR_REG  <= (others => '0');  -- only allow pulse during write strobe (single clock)
	end if;
end if;
end process INT_CLR_REG_GEN;


-- INTERRUPT OUTPUT

MASKED_INT <= INT_MASK_REG and INT_STAT_REG;

INT_OUTPUT_GEN: process (RST_I,CLK_I,PBEN_I,MASKED_INT) begin
if (RST_I = '1' or PBEN_I = '0') then	PBINT_O  <= '0';
elsif (CLK_I'event and CLK_I = '1') then
	if MASKED_INT = x"00000000" then PBINT_O <= '0';
	else PBINT_O <= '1';
	end if;
end if;
end process INT_OUTPUT_GEN;






end rtl;

