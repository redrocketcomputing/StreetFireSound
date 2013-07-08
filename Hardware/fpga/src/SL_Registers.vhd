----------------------------------------------------------------------------
--
--  File:   SL_Registers.vhd
--  Rev:    1.0.0
--  Date:	3-5-04
--
-- This is the VHDL module for the SLINK Register Interface for the SLINK Peripheral Module
-- in the Streetfire RBX Companion Chip (FPGA) for Streetfire Street Racer CPU Card to 
--  Application Board Interface
--
--	History:
--
--	Sync'd Intermodule Inputs WE_I,STB_I, 3-5-04	
--	Increased Clear Pulse to include ACK state, 3-5-04, REB
--	New Register Map and Function Definitions, 2-27-04,reb
--	Added RX FIFO Overflow Status, 2-24-04
--	Added TX FIFO COUNT registers, 2-19-04
-- 	Added B set of STatus/control registers for collision detection 2-17-04
--	Added Receive Register Read Pulse Output on each read 2-17-04
--	Created 2-6-04
----------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;

library unisim;
use unisim.ALL;

entity SL_Registers is 
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

-- PERIPHERAL MODULE SIGNALS
	PBEN_I		:in std_logic; -- module enable (active high)
	PBINT_O		:out std_logic;  -- interrupt output(active high)

-- SLINK SIGNALS
	SL_INT		:in std_logic;  -- 'or' of 32 interrupt sources
	SL_XDV		:out std_logic_vector(7 downto 0); -- SLINK Transmit data valid (pulse from register write)
	
-- SLINK REGISTER (I/O to logic)

	STAT_RO,	-- Status Register B (04) Bits (31:24)
	STAT_TF,	-- Status Register B (04) Bits (23:16)
	STAT_RE,	-- Status Register B (04) Bits (15:8)
	STAT_PI,	-- Status Register B (04) Bits (7:0)
	STAT_TS,	-- Status Register A (00) Bits (31:24)
	STAT_RS,	-- Status Register A (00) Bits (23:16)
	STAT_TC,	-- Status Register A (00) Bits (15:8)
	STAT_LI	-- Status Register A (00) Bits (7:0)
			:in std_logic_vector(7 downto 0); -- from Interrupt latches

	MASK_TS,	-- Mask Register A (08) Bits (31:24)
	MASK_RS,	-- Mask Register A (08) Bits (23:16)
	MASK_TC,	-- Mask Register A (08) Bits (15:8)
	MASK_LI	-- Mask Register A (08) Bits (7:0)
			:out std_logic_vector(7 downto 0); -- to Interrupt Logic

	CLR_TS,	-- Clear Register A (10) Bits (31:24)
	CLR_RS,	-- Clear Register A (10) Bits (23:16)
	CLR_TC,	-- Clear Register A (10) Bits (15:8)
	CLR_LI	-- Clear Register A (10) Bits (7:0)	
			:out std_logic_vector(7 downto 0); -- to interrupt logic
	CLR_TX_FIFO,	-- Control Register A (18) Bits (31:24)
	CLR_RX_FIFO,	-- Control Register A (18) Bits (23:16)
	ENA_TX,		-- Control Register A (18) Bits (15:8)
	ENA_RX		-- Control Register A (18) Bits (7:0)	
			:out std_logic_vector(7 downto 0); --  to TX/RX SLINK Logic
	SEND_SL0,	-- SL Tx Data Register (20) for SL TX Channel 0
	SEND_SL1,	-- SL Tx Data Register (24) for SL TX Channel 1
	SEND_SL2,	-- SL Tx Data Register (28) for SL TX Channel 2
	SEND_SL3,	-- SL Tx Data Register (2C) for SL TX Channel 3
	SEND_SL4,	-- SL Tx Data Register (30) for SL TX Channel 4
	SEND_SL5,	-- SL Tx Data Register (34) for SL TX Channel 5
	SEND_SL6,	-- SL Tx Data Register (38) for SL TX Channel 6
	SEND_SL7	-- SL Tx Data Register (3C) for SL TX Channel 7
			:out std_logic_vector(7 downto 0); -- Tx Data Registers (output to TX logic)	
	RCV_SL0,	-- SL RX Data Register (40) for SL RX Channel 0	
	RCV_SL1,	-- SL RX Data Register (44) for SL RX Channel 1
	RCV_SL2,	-- SL RX Data Register (48) for SL RX Channel 2
	RCV_SL3,	-- SL RX Data Register (4C) for SL RX Channel 3
	RCV_SL4,	-- SL RX Data Register (50) for SL RX Channel 4
	RCV_SL5,	-- SL RX Data Register (54) for SL RX Channel 5
	RCV_SL6,	-- SL RX Data Register (58) for SL RX Channel 6
	RCV_SL7		-- SL RX Data Register (5C) for SL RX Channel 7
			:in std_logic_vector(7 downto 0); -- Rx Data Registers (input from RX logic)
	READ_PULSE	:out std_logic_vector(7 downto 0); -- active high read pulse of one clock on read
	TXFIFO_CT0,
	TXFIFO_CT1,
	TXFIFO_CT2,
	TXFIFO_CT3,
	TXFIFO_CT4,
	TXFIFO_CT5,
	TXFIFO_CT6,
	TXFIFO_CT7, 
	RXFIFO_CT0,
	RXFIFO_CT1,
	RXFIFO_CT2,
	RXFIFO_CT3,
	RXFIFO_CT4,
	RXFIFO_CT5,
	RXFIFO_CT6,
	RXFIFO_CT7 	:in std_logic_vector(7 downto 0)
		);
end SL_Registers;


architecture rtl of SL_Registers is

-- REGISTER ADDRESSES: 
constant STAT_REG_ADDR_A: bit_vector (17 downto 0) := "000000000000000000"; -- 0x00
constant STAT_REG_ADDR_B: bit_vector (17 downto 0) := "000000000000000100"; -- 0x04
constant MASK_REG_ADDR_A: bit_vector (17 downto 0) := "000000000000001000"; -- 0x08
constant MASK_REG_ADDR_B: bit_vector (17 downto 0) := "000000000000001100"; -- 0x0C
constant CLR_REG_ADDR_A : bit_vector (17 downto 0) := "000000000000010000"; -- 0x10
constant CLR_REG_ADDR_B : bit_vector (17 downto 0) := "000000000000010100"; -- 0x14
constant CTRL_REG_ADDR_A: bit_vector (17 downto 0) := "000000000000011000"; -- 0x18
constant CTRL_REG_ADDR_B: bit_vector (17 downto 0) := "000000000000011100"; -- 0x1C
constant SEND_SL0_ADDR: bit_vector (17 downto 0) := "000000000000100000"; -- 0x20
constant SEND_SL1_ADDR: bit_vector (17 downto 0) := "000000000000100100"; -- 0x24
constant SEND_SL2_ADDR: bit_vector (17 downto 0) := "000000000000101000"; -- 0x28
constant SEND_SL3_ADDR: bit_vector (17 downto 0) := "000000000000101100"; -- 0x2C
constant SEND_SL4_ADDR: bit_vector (17 downto 0) := "000000000000110000"; -- 0x30
constant SEND_SL5_ADDR: bit_vector (17 downto 0) := "000000000000110100"; -- 0x34
constant SEND_SL6_ADDR: bit_vector (17 downto 0) := "000000000000111000"; -- 0x38
constant SEND_SL7_ADDR: bit_vector (17 downto 0) := "000000000000111100"; -- 0x3C
constant RCV_SL0_ADDR : bit_vector (17 downto 0) := "000000000001000000"; -- 0x40
constant RCV_SL1_ADDR : bit_vector (17 downto 0) := "000000000001000100"; -- 0x44
constant RCV_SL2_ADDR : bit_vector (17 downto 0) := "000000000001001000"; -- 0x48
constant RCV_SL3_ADDR : bit_vector (17 downto 0) := "000000000001001100"; -- 0x4C
constant RCV_SL4_ADDR : bit_vector (17 downto 0) := "000000000001010000"; -- 0x50
constant RCV_SL5_ADDR : bit_vector (17 downto 0) := "000000000001010100"; -- 0x54
constant RCV_SL6_ADDR : bit_vector (17 downto 0) := "000000000001011000"; -- 0x58
constant RCV_SL7_ADDR : bit_vector (17 downto 0) := "000000000001011100"; -- 0x5C


-- SIGNAL DEFINITIONS:
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

-- Module Specific Signals
signal 	SL_XDV_out	: std_logic_vector(7 downto 0);

signal 	STAT_RO_out	:std_logic_vector(7 downto 0);
signal 	STAT_TF_out	:std_logic_vector(7 downto 0);
signal 	STAT_RE_out	:std_logic_vector(7 downto 0);
signal 	STAT_PI_out	:std_logic_vector(7 downto 0);

signal 	STAT_TS_out	:std_logic_vector(7 downto 0);
signal 	STAT_RS_out	:std_logic_vector(7 downto 0);
signal 	STAT_TC_out	:std_logic_vector(7 downto 0);
signal 	STAT_LI_out	:std_logic_vector(7 downto 0);
signal 	MASK_TS_out	:std_logic_vector(7 downto 0);
signal 	MASK_RS_out	:std_logic_vector(7 downto 0);
signal 	MASK_TC_out	:std_logic_vector(7 downto 0);
signal 	MASK_LI_out	:std_logic_vector(7 downto 0);
signal 	CLR_TS_out	:std_logic_vector(7 downto 0);
signal 	CLR_RS_out	:std_logic_vector(7 downto 0);
signal 	CLR_TC_out	:std_logic_vector(7 downto 0);
signal 	CLR_LI_out	:std_logic_vector(7 downto 0);
signal 	CLR_TX_FIFO_out: std_logic_vector(7 downto 0);
signal 	CLR_RX_FIFO_out: std_logic_vector(7 downto 0);
signal 	ENA_TX_out	:std_logic_vector(7 downto 0);
signal 	ENA_RX_out	:std_logic_vector(7 downto 0);
signal 	SEND_SL0_out	:std_logic_vector(7 downto 0);
signal 	SEND_SL1_out	:std_logic_vector(7 downto 0);
signal 	SEND_SL2_out	:std_logic_vector(7 downto 0);
signal 	SEND_SL3_out	:std_logic_vector(7 downto 0);
signal 	SEND_SL4_out	:std_logic_vector(7 downto 0);
signal 	SEND_SL5_out	:std_logic_vector(7 downto 0);
signal 	SEND_SL6_out	:std_logic_vector(7 downto 0);
signal 	SEND_SL7_out	:std_logic_vector(7 downto 0);
signal 	RCV_SL0_out	:std_logic_vector(7 downto 0);
signal 	RCV_SL1_out	:std_logic_vector(7 downto 0);
signal 	RCV_SL2_out	:std_logic_vector(7 downto 0);
signal 	RCV_SL3_out	:std_logic_vector(7 downto 0);
signal 	RCV_SL4_out	:std_logic_vector(7 downto 0);
signal 	RCV_SL5_out	:std_logic_vector(7 downto 0);
signal 	RCV_SL6_out	:std_logic_vector(7 downto 0);
signal 	RCV_SL7_out	:std_logic_vector(7 downto 0);


signal TXFIFO_CT0_out,
	TXFIFO_CT1_out,
	TXFIFO_CT2_out,
	TXFIFO_CT3_out,
	TXFIFO_CT4_out,
	TXFIFO_CT5_out,
	TXFIFO_CT6_out,
	TXFIFO_CT7_out : std_logic_vector(7 downto 0);
signal RXFIFO_CT0_out,
	RXFIFO_CT1_out,
	RXFIFO_CT2_out,
	RXFIFO_CT3_out,
	RXFIFO_CT4_out,
	RXFIFO_CT5_out,
	RXFIFO_CT6_out,
	RXFIFO_CT7_out : std_logic_vector(7 downto 0);


signal LADR_I		:std_logic_vector(17 downto 0); -- peripheral address latched
signal WRITE_ACK_SV : std_logic; -- state indicator for write ack state

signal READ_PULSE_out : std_logic_vector(7 downto 0); -- single active high pulse on read of rcv reg
signal READ_ACK_SV  : std_logic; -- state indicator for read ack state

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


--###########################################################
--###  Standard Slave Interface #############################
--###########################################################

--**********************************************************
-- STATE MACHINE
--**********************************************************
--SLAVE STATE MACHINE SYNCHRONIZATION 
SLAVE_currentStateProc: process(RST_I, CLK_I,PBEN_I, SLAVE_nextState) begin
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


--**********************************************************
-- ACK_O output: 
--**********************************************************
ACK_O_GEN: process (SLAVE_currentState)
begin
case SLAVE_currentState is
when WRITE_ACK 	=> ACK_O <= '1';
when READ_ACK  	=> ACK_O <= '1';
when others 	=> ACK_O <= '0';
end case;
end process ACK_O_GEN;
--**********************************************************
--###########################################################


-- ADR_I Latch (on idle with STB)
LADR_I_GEN: process(CLK_I,RST_I,PBEN_I,SLAVE_currentState,ADR_I,LADR_I) begin  
if (RST_I = '1' or PBEN_I = '0') then LADR_I <= ADR_I;
elsif (CLK_I'event and CLK_I = '1') then 
	case SLAVE_currentState is
		when SLAVE_IDLE	=> LADR_I <= ADR_I;
		when others 	=> LADR_I <= LADR_I;
	end case;
end if;
end process LADR_I_GEN;


--###########################################################
--###  Module Specific Logic    #############################
--###########################################################


--**********************************************************
-- DATA OUTPUT MUX
--**********************************************************
ADR_I_BV <= to_bitvector(LADR_I);

-- DAT_O output:
DAT_O_GEN: process (ADR_I_BV,
		STAT_TS_out,STAT_RS_out,STAT_TC_out,STAT_LI_out,
		STAT_RO_out,STAT_TF_out,STAT_RE_out, STAT_PI_out, 
		MASK_TS_out,MASK_RS_out,MASK_TC_out,MASK_LI_out,		
		ENA_TX_out,ENA_RX_out,
		SEND_SL0_out,SEND_SL1_out,SEND_SL2_out,SEND_SL3_out,
		SEND_SL4_out,SEND_SL5_out,SEND_SL6_out,SEND_SL7_out,
		RCV_SL0_out,RCV_SL1_out,RCV_SL2_out,RCV_SL3_out,
		RCV_SL4_out,RCV_SL5_out,RCV_SL6_out,RCV_SL7_out,
		CLR_TX_FIFO_out,CLR_RX_FIFO_out,
		RXFIFO_CT0_out,RXFIFO_CT1_out,RXFIFO_CT2_out,
		RXFIFO_CT3_out,RXFIFO_CT4_out,RXFIFO_CT5_out,
		RXFIFO_CT6_out,	RXFIFO_CT7_out	,
		TXFIFO_CT0_out,TXFIFO_CT1_out,TXFIFO_CT2_out,
		TXFIFO_CT3_out,TXFIFO_CT4_out,TXFIFO_CT5_out,
		TXFIFO_CT6_out,	TXFIFO_CT7_out	)
begin
case ADR_I_BV is
when	STAT_REG_ADDR_A	 => 	
			DAT_O(31 downto 24) <= STAT_TS_out;
			DAT_O(23 downto 16) <=	STAT_RS_out; 
			DAT_O(15 downto 8)  <=	STAT_TC_out; 
			DAT_O(7 downto 0)   <=	STAT_LI_out; -- 0x0
when	STAT_REG_ADDR_B	 => 	
			DAT_O(31 downto 24) <=  STAT_RO_out;
			DAT_O(23 downto 16) <=	STAT_TF_out; 
			DAT_O(15 downto 8)   <=	STAT_RE_out; -- 0x4
			DAT_O(7 downto 0)   <=	STAT_PI_out; -- 0x4
when	MASK_REG_ADDR_A	 => 
			DAT_O(31 downto 24) <= MASK_TS_out;
			DAT_O(23 downto 16) <=	MASK_RS_out; 
			DAT_O(15 downto 8)  <=	MASK_TC_out; 
			DAT_O(7 downto 0)   <=	MASK_LI_out; -- 0x8
when	CTRL_REG_ADDR_A	 => 
			DAT_O(31 downto 24) <= CLR_TX_FIFO_out; 
			DAT_O(23 downto 16) <=	CLR_RX_FIFO_out; --2-16-04 for fifo support
			DAT_O(15 downto 8)  <=	ENA_TX_out; 
			DAT_O(7 downto 0)   <=	ENA_RX_out; -- 0x18
when	SEND_SL0_ADDR	 => 
			DAT_O(31 downto 24)  <= TXFIFO_CT0_out; -- FIFO CT 
			DAT_O(23 downto 8)  <= (others => '0'); -- reserved bits  		
			DAT_O(7 downto 0)   <=	SEND_SL0_out;  	 -- 0x20
when	SEND_SL1_ADDR	 =>
			DAT_O(31 downto 24)  <= TXFIFO_CT1_out; -- FIFO CT 
			DAT_O(23 downto 8)  <= (others => '0'); -- reserved bits  		
			DAT_O(7 downto 0)   <= SEND_SL1_out	;	 -- 0x24
when	SEND_SL2_ADDR	 =>
			DAT_O(31 downto 24)  <= TXFIFO_CT2_out; -- FIFO CT 
			DAT_O(23 downto 8)  <= (others => '0'); -- reserved bits  		
			DAT_O(7 downto 0)   <=	SEND_SL2_out;	 -- 0x28
when	SEND_SL3_ADDR	 =>
			DAT_O(31 downto 24)  <= TXFIFO_CT3_out; -- FIFO CT 
			DAT_O(23 downto 8)  <= (others => '0'); -- reserved bits  		
			DAT_O(7 downto 0)   <=	SEND_SL3_out;	 -- 0x2C
when	SEND_SL4_ADDR	 =>
			DAT_O(31 downto 24)  <= TXFIFO_CT4_out; -- FIFO CT 
			DAT_O(23 downto 8)  <= (others => '0'); -- reserved bits  		
			DAT_O(7 downto 0)   <=	SEND_SL4_out;	 -- 0x30
when	SEND_SL5_ADDR	 =>
			DAT_O(31 downto 24)  <= TXFIFO_CT5_out; -- FIFO CT 
			DAT_O(23 downto 8)  <= (others => '0'); -- reserved bits  		
			DAT_O(7 downto 0)   <=	SEND_SL5_out;	 -- 0x34
when	SEND_SL6_ADDR	 =>
			DAT_O(31 downto 24)  <= TXFIFO_CT6_out; -- FIFO CT 
			DAT_O(23 downto 8)  <= (others => '0'); -- reserved bits  		
			DAT_O(7 downto 0)   <=	SEND_SL6_out;	 -- 0x38
when	SEND_SL7_ADDR	 =>
			DAT_O(31 downto 24)  <= TXFIFO_CT7_out; -- FIFO CT 
			DAT_O(23 downto 8)  <= (others => '0'); -- reserved bits  		
			DAT_O(7 downto 0)   <=	SEND_SL7_out;	 -- 0x3C
when	RCV_SL0_ADDR	 =>
			DAT_O(31 downto 24)  <= RXFIFO_CT0_out; -- FIFO CT 
			DAT_O(23 downto 8)  <= (others => '0'); -- reserved bits  			
			DAT_O(7 downto 0)   <=	RCV_SL0_out;	 -- 0x40
when	RCV_SL1_ADDR	 =>
			DAT_O(31 downto 24)  <= RXFIFO_CT1_out; -- FIFO CT 
			DAT_O(23 downto 8)  <= (others => '0'); -- reserved bits  			
 			DAT_O(7 downto 0)   <=	RCV_SL1_out;	 -- 0x44
when	RCV_SL2_ADDR	 =>
			DAT_O(31 downto 24)  <= RXFIFO_CT2_out; -- FIFO CT 
			DAT_O(23 downto 8)  <= (others => '0'); -- reserved bits  
			DAT_O(7 downto 0)   <=	RCV_SL2_out;	 -- 0x48
when	RCV_SL3_ADDR	 =>
			DAT_O(31 downto 24)  <= RXFIFO_CT3_out; -- FIFO CT 
			DAT_O(23 downto 8)  <= (others => '0'); -- reserved bits  
			DAT_O(7 downto 0)   <=	RCV_SL3_out;	 -- 0x4C
when	RCV_SL4_ADDR	 =>
			DAT_O(31 downto 24)  <= RXFIFO_CT4_out; -- FIFO CT 
			DAT_O(23 downto 8)  <= (others => '0'); -- reserved bits  
			DAT_O(7 downto 0)   <=	RCV_SL4_out;	 -- 0x50
when	RCV_SL5_ADDR	 =>
			DAT_O(31 downto 24)  <= RXFIFO_CT5_out; -- FIFO CT 
			DAT_O(23 downto 8)  <= (others => '0'); -- reserved bits  
			DAT_O(7 downto 0)   <=	RCV_SL5_out;	 -- 0x54
when	RCV_SL6_ADDR	 =>
			DAT_O(31 downto 24)  <= RXFIFO_CT6_out; -- FIFO CT 
			DAT_O(23 downto 8)  <= (others => '0'); -- reserved bits  
			DAT_O(7 downto 0)   <=	RCV_SL6_out;	 -- 0x58
when	RCV_SL7_ADDR	 =>
			DAT_O(31 downto 24)  <= RXFIFO_CT7_out; -- FIFO CT 
			DAT_O(23 downto 8)  <= (others => '0'); -- reserved bits  
			DAT_O(7 downto 0)   <=	RCV_SL7_out;	 -- 0x5C
when 	others		 => 
			DAT_O   <= x"ABADBABE"   ;        --invalid address
end case;
end process DAT_O_GEN;
--**********************************************************

-- REGISTER IMPLEMENTATION:


-- Write Only Registers:
--**********************************************************
-- Interrupt Clear Register A: 0x10  (Write Only)
--**********************************************************
INT_CLR_REG_A_GEN: process (PBEN_I,RST_I, CLK_I,SEL_I,SLAVE_currentState, ADR_I_BV, DAT_I,
						CLR_TS_out,CLR_RS_out,CLR_TC_out,CLR_LI_out)
begin 
if (RST_I = '1'or PBEN_I = '0') then	
		CLR_TS_out  <= (others => '0');
		CLR_RS_out  <= (others => '0');
		CLR_TC_out  <= (others => '0');
		CLR_LI_out  <= (others => '0');
elsif (CLK_I'event and CLK_I = '1') then 
	if (SLAVE_currentState = WRITE_STROBE) and (ADR_I_BV = CLR_REG_ADDR_A) then
		if SEL_I(3) = '1' then CLR_TS_out <= DAT_I(31 downto 24);
		end if;
		if SEL_I(2) = '1' then CLR_RS_out <= DAT_I(23 downto 16);
		end if;
		if SEL_I(1) = '1' then CLR_TC_out <= DAT_I(15 downto 8);
		end if;
		if SEL_I(0) = '1' then CLR_LI_out <= DAT_I(7 downto 0);
		end if;
-----------------------------------------------------------------------
--3-5-04 Extended clear pulse through WRITE_ACK state, REB
	elsif (SLAVE_currentState = WRITE_ACK) and (ADR_I_BV = CLR_REG_ADDR_A) then
		if SEL_I(3) = '1' then CLR_TS_out <= CLR_TS_out ;
		end if;
		if SEL_I(2) = '1' then CLR_RS_out <= CLR_RS_out ;
		end if;
		if SEL_I(1) = '1' then CLR_TC_out <= CLR_TC_out ;
		end if;
		if SEL_I(0) = '1' then CLR_LI_out <= CLR_LI_out ;

		end if;
-----------------------------------------------------------------------

	else 	
		CLR_TS_out  <= (others => '0');  -- only allow pulse during write strobe (single clock)
		CLR_RS_out  <= (others => '0');
		CLR_TC_out  <= (others => '0');
		CLR_LI_out  <= (others => '0');
	end if;
end if;
end process INT_CLR_REG_A_GEN;

CLR_TS	<=	CLR_TS_out	;
CLR_RS	<=	CLR_RS_out	;
CLR_TC	<=	CLR_TC_out	;
CLR_LI	<=	CLR_LI_out	;
----------------------------------------------------------------------


--**********************************************************
-- Read/Write Registers:
--**********************************************************
-- SL Interrupt MASK Register A: 0x08  (R/W)
--**********************************************************
MASK_REG_A_GEN: process (PBEN_I,RST_I, CLK_I,SEL_I,SLAVE_currentState, ADR_I_BV, DAT_I)
begin 
if (RST_I = '1'or PBEN_I = '0') then	
		MASK_TS_out  <= (others => '0');
		MASK_RS_out  <= (others => '0');
		MASK_TC_out  <= (others => '0');
		MASK_LI_out  <= (others => '0');
elsif (CLK_I'event and CLK_I = '1') then 
	if (SLAVE_currentState = WRITE_STROBE) and (ADR_I_BV = MASK_REG_ADDR_A) then
		if SEL_I(3) = '1' then MASK_TS_out <= DAT_I(31 downto 24);
		end if;
		if SEL_I(2) = '1' then MASK_RS_out <= DAT_I(23 downto 16);
		end if;
		if SEL_I(1) = '1' then MASK_TC_out <= DAT_I(15 downto 8);
		end if;
		if SEL_I(0) = '1' then MASK_LI_out <= DAT_I(7 downto 0);
		end if;
	end if;
end if;
end process MASK_REG_A_GEN;

MASK_TS	<=	MASK_TS_out	;
MASK_RS	<=	MASK_RS_out	;
MASK_TC	<=	MASK_TC_out	;
MASK_LI	<=	MASK_LI_out	;
--**********************************************************

--**********************************************************
-- SL Control Register A: 0x18  (R/W)
--**********************************************************
CTRL_REG_A_GEN: process (PBEN_I,RST_I, CLK_I,SEL_I,SLAVE_currentState, ADR_I_BV, DAT_I)
begin 
if (RST_I = '1'or PBEN_I = '0') then
		CLR_TX_FIFO_out <= (others => '0');
		CLR_RX_FIFO_out <= (others => '0');
		ENA_TX_out  <= (others => '0');
		ENA_RX_out  <= (others => '0');
elsif (CLK_I'event and CLK_I = '1') then 
	if (SLAVE_currentState = WRITE_STROBE) and (ADR_I_BV = CTRL_REG_ADDR_A) then
		if SEL_I(3) = '1' then CLR_TX_FIFO_out <= DAT_I(31 downto 24);
		end if;
		if SEL_I(2) = '1' then CLR_RX_FIFO_out <= DAT_I(23 downto 16);
		end if;
		if SEL_I(1) = '1' then ENA_TX_out <= DAT_I(15 downto 8);
		end if;
		if SEL_I(0) = '1' then ENA_RX_out <= DAT_I(7 downto 0);
		end if;
	end if;
end if;
end process CTRL_REG_A_GEN;

CLR_RX_FIFO <= CLR_RX_FIFO_out ;
CLR_TX_FIFO <= CLR_TX_FIFO_out ;
ENA_TX	<=	ENA_TX_out	;
ENA_RX	<=	ENA_RX_out	;
--**********************************************************

--**********************************************************
-- SLINK SEND Registers: 0x20 - 0x3C (R/W)
--**********************************************************
SEND_SL0_REG_GEN: process (PBEN_I,RST_I, CLK_I,SEL_I,SLAVE_currentState, ADR_I_BV, DAT_I)
begin 
if (RST_I = '1'or PBEN_I = '0') then	
		SEND_SL0_out  <= (others => '0');
elsif (CLK_I'event and CLK_I = '1') then 
	if (SLAVE_currentState = WRITE_STROBE) and (ADR_I_BV = SEND_SL0_ADDR) then
		if SEL_I(0) = '1' then SEND_SL0_out <= DAT_I(7 downto 0);
		end if;
	end if;
end if;
end process SEND_SL0_REG_GEN;

SEND_SL0	<=	SEND_SL0_out	;-- 0x20
--**********************************************************

SEND_SL1_REG_GEN: process (PBEN_I,RST_I, CLK_I,SEL_I,SLAVE_currentState, ADR_I_BV, DAT_I)
begin 
if (RST_I = '1'or PBEN_I = '0') then	
		SEND_SL1_out  <= (others => '0');
elsif (CLK_I'event and CLK_I = '1') then 
	if (SLAVE_currentState = WRITE_STROBE) and (ADR_I_BV = SEND_SL1_ADDR) then
		if SEL_I(0) = '1' then SEND_SL1_out <= DAT_I(7 downto 0);
		end if;
	end if;
end if;
end process SEND_SL1_REG_GEN;

SEND_SL1	<=	SEND_SL1_out	;-- 0x24
--**********************************************************

SEND_SL2_REG_GEN: process (PBEN_I,RST_I, CLK_I,SEL_I,SLAVE_currentState, ADR_I_BV, DAT_I)
begin 
if (RST_I = '1'or PBEN_I = '0') then	
		SEND_SL2_out  <= (others => '0');
elsif (CLK_I'event and CLK_I = '1') then 
	if (SLAVE_currentState = WRITE_STROBE) and (ADR_I_BV = SEND_SL2_ADDR) then
		if SEL_I(0) = '1' then SEND_SL2_out <= DAT_I(7 downto 0);
		end if;
	end if;
end if;
end process SEND_SL2_REG_GEN;

SEND_SL2	<=	SEND_SL2_out	;-- 0x28
--**********************************************************

SEND_SL3_REG_GEN: process (PBEN_I,RST_I, CLK_I,SEL_I,SLAVE_currentState, ADR_I_BV, DAT_I)
begin 
if (RST_I = '1'or PBEN_I = '0') then	
		SEND_SL3_out  <= (others => '0');
elsif (CLK_I'event and CLK_I = '1') then 
	if (SLAVE_currentState = WRITE_STROBE) and (ADR_I_BV = SEND_SL3_ADDR) then
		if SEL_I(0) = '1' then SEND_SL3_out <= DAT_I(7 downto 0);
		end if;
	end if;
end if;
end process SEND_SL3_REG_GEN;

SEND_SL3	<=	SEND_SL3_out	;-- 0x2C
--**********************************************************

SEND_SL4_REG_GEN: process (PBEN_I,RST_I, CLK_I,SEL_I,SLAVE_currentState, ADR_I_BV, DAT_I)
begin 
if (RST_I = '1'or PBEN_I = '0') then	
		SEND_SL4_out  <= (others => '0');
elsif (CLK_I'event and CLK_I = '1') then 
	if (SLAVE_currentState = WRITE_STROBE) and (ADR_I_BV = SEND_SL4_ADDR) then
		if SEL_I(0) = '1' then SEND_SL4_out <= DAT_I(7 downto 0);
		end if;
	end if;
end if;
end process SEND_SL4_REG_GEN;

SEND_SL4	<=	SEND_SL4_out	;-- 0x30
--**********************************************************

SEND_SL5_REG_GEN: process (PBEN_I,RST_I, CLK_I,SEL_I,SLAVE_currentState, ADR_I_BV, DAT_I)
begin 
if (RST_I = '1'or PBEN_I = '0') then	
		SEND_SL5_out  <= (others => '0');
elsif (CLK_I'event and CLK_I = '1') then 
	if (SLAVE_currentState = WRITE_STROBE) and (ADR_I_BV = SEND_SL5_ADDR) then
		if SEL_I(0) = '1' then SEND_SL5_out <= DAT_I(7 downto 0);
		end if;
	end if;
end if;
end process SEND_SL5_REG_GEN;

SEND_SL5	<=	SEND_SL5_out	;-- 0x34
--**********************************************************

SEND_SL6_REG_GEN: process (PBEN_I,RST_I, CLK_I,SEL_I,SLAVE_currentState, ADR_I_BV, DAT_I)
begin 
if (RST_I = '1'or PBEN_I = '0') then	
		SEND_SL6_out  <= (others => '0');
elsif (CLK_I'event and CLK_I = '1') then 
	if (SLAVE_currentState = WRITE_STROBE) and (ADR_I_BV = SEND_SL6_ADDR) then
		if SEL_I(0) = '1' then SEND_SL6_out <= DAT_I(7 downto 0);
		end if;
	end if;
end if;
end process SEND_SL6_REG_GEN;

SEND_SL6	<=	SEND_SL6_out	;-- 0x38
--**********************************************************

SEND_SL7_REG_GEN: process (PBEN_I,RST_I, CLK_I,SEL_I,SLAVE_currentState, ADR_I_BV, DAT_I)
begin 
if (RST_I = '1'or PBEN_I = '0') then	
		SEND_SL7_out  <= (others => '0');
elsif (CLK_I'event and CLK_I = '1') then 
	if (SLAVE_currentState = WRITE_STROBE) and (ADR_I_BV = SEND_SL7_ADDR) then
		if SEL_I(0) = '1' then SEND_SL7_out <= DAT_I(7 downto 0);
		end if;
	end if;
end if;
end process SEND_SL7_REG_GEN;

SEND_SL7	<=	SEND_SL7_out	;-- 0x3C
--**********************************************************

-- Read Only Registers:
--**********************************************************
-- SLINK STATUS Register A: 0x00  (Read Only)
--**********************************************************
STAT_TS_GEN: process(STAT_TS) begin  STAT_TS_out <= STAT_TS;
end process STAT_TS_GEN;

STAT_RS_GEN: process(STAT_RS) begin  STAT_RS_out <= STAT_RS;
end process STAT_RS_GEN;

STAT_TC_GEN: process(STAT_TC) begin  STAT_TC_out <= STAT_TC;
end process STAT_TC_GEN;

STAT_LI_GEN: process(STAT_LI) begin  STAT_LI_out <= STAT_LI;
end process STAT_LI_GEN;
--**********************************************************

--**********************************************************
-- SLINK STATUS Register B: 0x04  (Read Only)
--**********************************************************

STAT_RO_GEN: process(STAT_RO) begin  STAT_RO_out <= STAT_RO;
end process STAT_RO_GEN;

STAT_TF_GEN: process(STAT_TF) begin  STAT_TF_out <= STAT_TF;
end process STAT_TF_GEN;

STAT_PI_GEN: process(STAT_PI) begin  STAT_PI_out <= STAT_PI;
end process STAT_PI_GEN;

STAT_RE_GEN: process(STAT_RE) begin  STAT_RE_out <= STAT_RE;
end process STAT_RE_GEN;


--**********************************************************

--**********************************************************
-- SLINK RECEIVE Registers: 0x40 - 0x5C  (Read Only)
--**********************************************************
RCV_SL0_GEN: process(RCV_SL0) begin  RCV_SL0_out <= RCV_SL0;
end process RCV_SL0_GEN; -- 0x40

RCV_SL1_GEN: process(RCV_SL1) begin  RCV_SL1_out <= RCV_SL1;
end process RCV_SL1_GEN; -- 0x44

RCV_SL2_GEN: process(RCV_SL2) begin  RCV_SL2_out <= RCV_SL2;
end process RCV_SL2_GEN; -- 0x48

RCV_SL3_GEN: process(RCV_SL3) begin  RCV_SL3_out <= RCV_SL3;
end process RCV_SL3_GEN; -- 0x4C

RCV_SL4_GEN: process(RCV_SL4) begin  RCV_SL4_out <= RCV_SL4;
end process RCV_SL4_GEN; -- 0x50

RCV_SL5_GEN: process(RCV_SL5) begin  RCV_SL5_out <= RCV_SL5;
end process RCV_SL5_GEN; -- 0x54

RCV_SL6_GEN: process(RCV_SL6) begin  RCV_SL6_out <= RCV_SL6;
end process RCV_SL6_GEN; -- 0x58

RCV_SL7_GEN: process(RCV_SL7) begin  RCV_SL7_out <= RCV_SL7;
end process RCV_SL7_GEN; -- 0x5C
--**********************************************************
--**********************************************************
-- RX FIFO COUNT Registers: 0x40 - 0x5C  (Read Only)
--**********************************************************
RXFIFO_CT0_GEN: process(RXFIFO_CT0) begin  RXFIFO_CT0_out <= RXFIFO_CT0;
end process RXFIFO_CT0_GEN; -- 0x40

RXFIFO_CT1_GEN: process(RXFIFO_CT1) begin  RXFIFO_CT1_out <= RXFIFO_CT1;
end process RXFIFO_CT1_GEN; -- 0x44

RXFIFO_CT2_GEN: process(RXFIFO_CT2) begin  RXFIFO_CT2_out <= RXFIFO_CT2;
end process RXFIFO_CT2_GEN; -- 0x48

RXFIFO_CT3_GEN: process(RXFIFO_CT3) begin  RXFIFO_CT3_out <= RXFIFO_CT3;
end process RXFIFO_CT3_GEN; -- 0x4C

RXFIFO_CT4_GEN: process(RXFIFO_CT4) begin  RXFIFO_CT4_out <= RXFIFO_CT4;
end process RXFIFO_CT4_GEN; -- 0x50

RXFIFO_CT5_GEN: process(RXFIFO_CT5) begin  RXFIFO_CT5_out <= RXFIFO_CT5;
end process RXFIFO_CT5_GEN; -- 0x54

RXFIFO_CT6_GEN: process(RXFIFO_CT6) begin  RXFIFO_CT6_out <= RXFIFO_CT6;
end process RXFIFO_CT6_GEN; -- 0x58

RXFIFO_CT7_GEN: process(RXFIFO_CT7) begin  RXFIFO_CT7_out <= RXFIFO_CT7;
end process RXFIFO_CT7_GEN; -- 0x5C
--**********************************************************
--**********************************************************
-- TX FIFO COUNT Registers: 0x20 - 0x3C  (Read Only)
--**********************************************************
TXFIFO_CT0_GEN: process(TXFIFO_CT0) begin  TXFIFO_CT0_out <= TXFIFO_CT0;
end process TXFIFO_CT0_GEN; -- 0x40

TXFIFO_CT1_GEN: process(TXFIFO_CT1) begin  TXFIFO_CT1_out <= TXFIFO_CT1;
end process TXFIFO_CT1_GEN; -- 0x44

TXFIFO_CT2_GEN: process(TXFIFO_CT2) begin  TXFIFO_CT2_out <= TXFIFO_CT2;
end process TXFIFO_CT2_GEN; -- 0x48

TXFIFO_CT3_GEN: process(TXFIFO_CT3) begin  TXFIFO_CT3_out <= TXFIFO_CT3;
end process TXFIFO_CT3_GEN; -- 0x4C

TXFIFO_CT4_GEN: process(TXFIFO_CT4) begin  TXFIFO_CT4_out <= TXFIFO_CT4;
end process TXFIFO_CT4_GEN; -- 0x50

TXFIFO_CT5_GEN: process(TXFIFO_CT5) begin  TXFIFO_CT5_out <= TXFIFO_CT5;
end process TXFIFO_CT5_GEN; -- 0x54

TXFIFO_CT6_GEN: process(TXFIFO_CT6) begin  TXFIFO_CT6_out <= TXFIFO_CT6;
end process TXFIFO_CT6_GEN; -- 0x58

TXFIFO_CT7_GEN: process(TXFIFO_CT7) begin  TXFIFO_CT7_out <= TXFIFO_CT7;
end process TXFIFO_CT7_GEN; -- 0x5C
--**********************************************************

--**********************************************************
-- OUTPUT ASSIGNMENTS:
--**********************************************************
PBINT_O	<= SL_INT;  -- Interrupt from Interrupt controller logic assigned to PB interrupt out


--**********************************************************
-- SL_XDV(k) OUTPUT GENERATION:, k= 0,1,2,...,7
--**********************************************************
--  SL_XDV <= SL_XDV_out
--  SL_XDV should pulse high one clock cycle following a write of any of the SL_SEND Registers:



SL_XDV0_Synch: process(RST_I, CLK_I,PBEN_I,WRITE_ACK_SV ,ADR_I_BV,STB_I, SL_XDV_out(0)) begin
if (RST_I = '1' or PBEN_I = '0') then SL_XDV_out(0)	<=	'0';
elsif (CLK_I'event and CLK_I = '1') then 
		if WRITE_ACK_SV = '1' and STB_I = '0' and (ADR_I_BV = SEND_SL0_ADDR) then SL_XDV_out(0)	<= '1';
		else SL_XDV_out(0)	<= '0';
		end if;
end if;
end process SL_XDV0_Synch;

SL_XDV1_Synch: process(RST_I, CLK_I,PBEN_I,WRITE_ACK_SV ,ADR_I_BV,STB_I, SL_XDV_out(1)) begin
if (RST_I = '1' or PBEN_I = '0') then SL_XDV_out(1)	<=	'0';
elsif (CLK_I'event and CLK_I = '1') then 
		if WRITE_ACK_SV = '1' and STB_I = '0' and (ADR_I_BV = SEND_SL1_ADDR) then SL_XDV_out(1)	<= '1';
		else SL_XDV_out(1)	<= '0';
		end if;
end if;
end process SL_XDV1_Synch;

SL_XDV2_Synch: process(RST_I, CLK_I,PBEN_I,WRITE_ACK_SV ,ADR_I_BV,STB_I, SL_XDV_out(2)) begin
if (RST_I = '1' or PBEN_I = '0') then SL_XDV_out(2)	<=	'0';
elsif (CLK_I'event and CLK_I = '1') then 
		if WRITE_ACK_SV = '1' and STB_I = '0' and (ADR_I_BV = SEND_SL2_ADDR) then SL_XDV_out(2)	<= '1';
		else SL_XDV_out(2)	<= '0';
		end if;
end if;
end process SL_XDV2_Synch;

SL_XDV3_Synch: process(RST_I, CLK_I,PBEN_I,WRITE_ACK_SV ,ADR_I_BV,STB_I, SL_XDV_out(3)) begin
if (RST_I = '1' or PBEN_I = '0') then SL_XDV_out(3)	<=	'0';
elsif (CLK_I'event and CLK_I = '1') then 
		if WRITE_ACK_SV = '1' and STB_I = '0' and (ADR_I_BV = SEND_SL3_ADDR) then SL_XDV_out(3)	<= '1';
		else SL_XDV_out(3)	<= '0';
		end if;
end if;
end process SL_XDV3_Synch;

SL_XDV4_Synch: process(RST_I, CLK_I,PBEN_I,WRITE_ACK_SV ,ADR_I_BV,STB_I, SL_XDV_out(4)) begin
if (RST_I = '1' or PBEN_I = '0') then SL_XDV_out(4)	<=	'0';
elsif (CLK_I'event and CLK_I = '1') then 
		if WRITE_ACK_SV = '1' and STB_I = '0' and (ADR_I_BV = SEND_SL4_ADDR) then SL_XDV_out(4)	<= '1';
		else SL_XDV_out(4)	<= '0';
		end if;
end if;
end process SL_XDV4_Synch;

SL_XDV5_Synch: process(RST_I, CLK_I,PBEN_I,WRITE_ACK_SV ,ADR_I_BV,STB_I, SL_XDV_out(5)) begin
if (RST_I = '1' or PBEN_I = '0') then SL_XDV_out(5)	<=	'0';
elsif (CLK_I'event and CLK_I = '1') then 
		if WRITE_ACK_SV = '1' and STB_I = '0' and (ADR_I_BV = SEND_SL5_ADDR) then SL_XDV_out(5)	<= '1';
		else SL_XDV_out(5)	<= '0';
		end if;
end if;
end process SL_XDV5_Synch;

SL_XDV6_Synch: process(RST_I, CLK_I,PBEN_I,WRITE_ACK_SV ,ADR_I_BV,STB_I, SL_XDV_out(6)) begin
if (RST_I = '1' or PBEN_I = '0') then SL_XDV_out(6)	<=	'0';
elsif (CLK_I'event and CLK_I = '1') then 
		if WRITE_ACK_SV = '1' and STB_I = '0' and (ADR_I_BV = SEND_SL6_ADDR) then SL_XDV_out(6)	<= '1';
		else SL_XDV_out(6)	<= '0';
		end if;
end if;
end process SL_XDV6_Synch;

SL_XDV7_Synch: process(RST_I, CLK_I,PBEN_I,WRITE_ACK_SV ,ADR_I_BV,STB_I, SL_XDV_out(7)) begin
if (RST_I = '1' or PBEN_I = '0') then SL_XDV_out(7)	<=	'0';
elsif (CLK_I'event and CLK_I = '1') then 
		if WRITE_ACK_SV = '1' and STB_I = '0' and (ADR_I_BV = SEND_SL7_ADDR) then SL_XDV_out(7)	<= '1';
		else SL_XDV_out(7)	<= '0';
		end if;
end if;
end process SL_XDV7_Synch;


SL_XDV	<=	SL_XDV_out;

WRITE_ACK_SV_GEN: process (SLAVE_currentState)
begin
case SLAVE_currentState is
when WRITE_ACK 	=> WRITE_ACK_SV <= '1';
when others 	=> WRITE_ACK_SV <= '0';
end case;
end process WRITE_ACK_SV_GEN;


--**********************************************************
-- READ_PULSE(k) OUTPUT GENERATION:, k= 0,1,2,...,7
--**********************************************************
  READ_PULSE <= READ_PULSE_out ;
--  READ_PULSE should pulse high one clock cycle following a read of any of the SL_RCV Registers:

READ_PULSE_0_Synch: process(RST_I, CLK_I,PBEN_I,READ_ACK_SV ,ADR_I_BV,STB_I) begin
if (RST_I = '1' or PBEN_I = '0') then READ_PULSE_out(0)	<=	'0';
elsif (CLK_I'event and CLK_I = '1') then 
		if READ_ACK_SV = '1' and STB_I = '0' and (ADR_I_BV = RCV_SL0_ADDR) then READ_PULSE_out(0) <= '1';
		else READ_PULSE_out(0)	<= '0';
		end if;
end if;
end process READ_PULSE_0_Synch;

READ_PULSE_1_Synch: process(RST_I, CLK_I,PBEN_I,READ_ACK_SV ,ADR_I_BV,STB_I) begin
if (RST_I = '1' or PBEN_I = '0') then READ_PULSE_out(1)	<=	'0';
elsif (CLK_I'event and CLK_I = '1') then 
		if READ_ACK_SV = '1' and STB_I = '0' and (ADR_I_BV = RCV_SL1_ADDR) then READ_PULSE_out(1) <= '1';
		else READ_PULSE_out(1)	<= '0';
		end if;
end if;
end process READ_PULSE_1_Synch;

READ_PULSE_2_Synch: process(RST_I, CLK_I,PBEN_I,READ_ACK_SV ,ADR_I_BV,STB_I) begin
if (RST_I = '1' or PBEN_I = '0') then READ_PULSE_out(2)	<=	'0';
elsif (CLK_I'event and CLK_I = '1') then 
		if READ_ACK_SV = '1' and STB_I = '0' and (ADR_I_BV = RCV_SL2_ADDR) then READ_PULSE_out(2) <= '1';
		else READ_PULSE_out(2)	<= '0';
		end if;
end if;
end process READ_PULSE_2_Synch;

READ_PULSE_3_Synch: process(RST_I, CLK_I,PBEN_I,READ_ACK_SV ,ADR_I_BV,STB_I) begin
if (RST_I = '1' or PBEN_I = '0') then READ_PULSE_out(3)	<=	'0';
elsif (CLK_I'event and CLK_I = '1') then 
		if READ_ACK_SV = '1' and STB_I = '0' and (ADR_I_BV = RCV_SL3_ADDR) then READ_PULSE_out(3) <= '1';
		else READ_PULSE_out(3)	<= '0';
		end if;
end if;
end process READ_PULSE_3_Synch;

READ_PULSE_4_Synch: process(RST_I, CLK_I,PBEN_I,READ_ACK_SV ,ADR_I_BV,STB_I) begin
if (RST_I = '1' or PBEN_I = '0') then READ_PULSE_out(4)	<=	'0';
elsif (CLK_I'event and CLK_I = '1') then 
		if READ_ACK_SV = '1' and STB_I = '0' and (ADR_I_BV = RCV_SL4_ADDR) then READ_PULSE_out(4) <= '1';
		else READ_PULSE_out(4)	<= '0';
		end if;
end if;
end process READ_PULSE_4_Synch;

READ_PULSE_5_Synch: process(RST_I, CLK_I,PBEN_I,READ_ACK_SV ,ADR_I_BV,STB_I) begin
if (RST_I = '1' or PBEN_I = '0') then READ_PULSE_out(5)	<=	'0';
elsif (CLK_I'event and CLK_I = '1') then 
		if READ_ACK_SV = '1' and STB_I = '0' and (ADR_I_BV = RCV_SL5_ADDR) then READ_PULSE_out(5) <= '1';
		else READ_PULSE_out(5)	<= '0';
		end if;
end if;
end process READ_PULSE_5_Synch;

READ_PULSE_6_Synch: process(RST_I, CLK_I,PBEN_I,READ_ACK_SV ,ADR_I_BV,STB_I) begin
if (RST_I = '1' or PBEN_I = '0') then READ_PULSE_out(6)	<=	'0';
elsif (CLK_I'event and CLK_I = '1') then 
		if READ_ACK_SV = '1' and STB_I = '0' and (ADR_I_BV = RCV_SL6_ADDR) then READ_PULSE_out(6) <= '1';
		else READ_PULSE_out(6)	<= '0';
		end if;
end if;
end process READ_PULSE_6_Synch;

READ_PULSE_7_Synch: process(RST_I, CLK_I,PBEN_I,READ_ACK_SV ,ADR_I_BV,STB_I) begin
if (RST_I = '1' or PBEN_I = '0') then READ_PULSE_out(7)	<=	'0';
elsif (CLK_I'event and CLK_I = '1') then 
		if READ_ACK_SV = '1' and STB_I = '0' and (ADR_I_BV = RCV_SL7_ADDR) then READ_PULSE_out(7) <= '1';
		else READ_PULSE_out(7)	<= '0';
		end if;
end if;
end process READ_PULSE_7_Synch;

   
READ_ACK_SV_GEN: process (SLAVE_currentState)
begin
case SLAVE_currentState is
when READ_ACK 	=> READ_ACK_SV <= '1';
when others 	=> READ_ACK_SV <= '0';
end case;
end process READ_ACK_SV_GEN;


--**********************************************************

end rtl;

