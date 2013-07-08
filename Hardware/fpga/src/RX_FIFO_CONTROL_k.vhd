----------------------------------------------------------------------------
--
--  File:   RX_FIFO_CONTROL_k.vhd
--  Rev:    1.0.0
--  Date:	3-4-04
--  This is the VHDL module for the control of one of the 8 Independent FIFOS for transmit channels
--  for the SLINK Peripheral Module in the Streetfire RBX Companion Chip (FPGA) for 
--  Streetfire Street Racer CPU Card to Application Board Interface.  
--
--  Author: Robyn E. Bauer
--
--
--	History: 
--	3-5-04 Added sync for intermodule inputs: SL_RDV,RXFIFO_EMPTY,READ_PULSE
--	Created 3-4-04 (modified from TX_FIFO_READER_k.vhd)
----------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;

library unisim;
use unisim.ALL;

entity RX_FIFO_CONTROL_k is
	generic(BV_RX_ALMOST_FULL_CT: bit_vector (7 downto 0)  ); 
	port ( 
	RST_I	:in std_logic; -- master reset for peripheral bus
	CLK_I	:in std_logic; -- master clock for peripheral bus
	ENA_RX  :in std_logic;  
	CLR_RX_FIFO : in std_logic; 
	SL_RDV : in std_logic;      
	RXFIFO_WRITE: out std_logic; --3-4	
	RXFIFO_READ: out std_logic;
	RXFIFO_EMPTY: in std_logic;
	RXFIFO_FULL: in std_logic;
	RX_READ: out std_logic;
	READ_PULSE: in std_logic;
	RXFIFO_ALMOST_FULL: out std_logic;
	RXFIFO_OVERFLOW: out std_logic;
	RXFIFO_DOUT:in std_logic_vector(7 downto 0);
	RXFIFO_COUNT:in std_logic_vector(7 downto 0);
	RCV_SL: out std_logic_vector(7 downto 0);
	RXFIFO_RE_EMPTY:out std_logic
	);
end RX_FIFO_CONTROL_k;

architecture rtl of RX_FIFO_CONTROL_k is

signal FIFO_DOUT_LATCH: std_logic_vector(7 downto 0);
signal LATCH_DOUT:std_logic;

signal RX_ALMOST_FULL_CT: std_logic_vector(7 downto 0);

type FREAD_StateType is (
FREAD_IDLE, 
FREAD_ENABLE,
FREAD_LATCH,
FREAD_HOLD);
signal FREAD_currentState, FREAD_nextState : FREAD_StateType;

signal SV_FREAD_IDLE: std_logic;

type FWRITE_StateType is (
FWRITE_IDLE, 
FWRITE_ENABLE,
FWRITE_WACK);
signal FWRITE_currentState, FWRITE_nextState : FWRITE_StateType;

signal SL_RDV_IN ,RXFIFO_EMPTY_IN,READ_PULSE_IN:std_logic;

--**********************************************************
--  LOGIC BEGINS HERE:
--**********************************************************
  
begin

-------------------------------------------------
--3-5-04 Added intermodule sync for SL_RDV,RXFIFO_EMPTY,READ_PULSE
-------------------------------------------------
SL_RDV_IN_GEN: process (CLK_I,SL_RDV) 
begin 
if (CLK_I 'event and CLK_I = '1') then SL_RDV_IN <=SL_RDV;
end if;
end process SL_RDV_IN_GEN;
-------------------------------------------------
RXFIFO_EMPTY_IN_GEN: process (CLK_I,RXFIFO_EMPTY) 
begin 
if (CLK_I 'event and CLK_I = '1') then RXFIFO_EMPTY_IN <= RXFIFO_EMPTY;
end if;
end process RXFIFO_EMPTY_IN_GEN;
-------------------------------------------------
READ_PULSE_IN_GEN: process (CLK_I,READ_PULSE) 
begin 
if (CLK_I 'event and CLK_I = '1') then READ_PULSE_IN <=READ_PULSE;
end if;
end process READ_PULSE_IN_GEN;
-------------------------------------------------



--FIFO READ LOGIC:

--**********************************************************
-- FIFO_DOUT_LATCH:
--**********************************************************

RCV_SL <= FIFO_DOUT_LATCH;

FIFO_DOUT_LATCH_GEN: process (CLK_I, RST_I, LATCH_DOUT,RXFIFO_DOUT, FIFO_DOUT_LATCH)
begin

if (RST_I = '1' ) then FIFO_DOUT_LATCH <= (others => '0');
elsif (CLK_I'event and CLK_I = '1') then 
	if LATCH_DOUT = '1' then  
		FIFO_DOUT_LATCH <= RXFIFO_DOUT;
	else FIFO_DOUT_LATCH <= FIFO_DOUT_LATCH;
	end if;
end if;
end process FIFO_DOUT_LATCH_GEN;
----------------------------------------------------------------
--**********************************************************
-- RXFIFO_STATUS_DECODE: RXFIFO_ALMOST_FULL, RXFIFO_OVERFLOW, and RXFIFO_RE_EMPTY
--**********************************************************
RX_ALMOST_FULL_CT <= to_stdlogicvector(BV_RX_ALMOST_FULL_CT);

RXFIFO_ALMOST_FULL_GEN: process (RXFIFO_COUNT,RX_ALMOST_FULL_CT)
begin
if RXFIFO_COUNT < RX_ALMOST_FULL_CT then RXFIFO_ALMOST_FULL <= '0';
else RXFIFO_ALMOST_FULL <= '1';
end if;
end process RXFIFO_ALMOST_FULL_GEN;


RXFIFO_OVERFLOW <= RXFIFO_FULL; -- USE FIFO OUTPUT FOR NOW

RXFIFO_RE_EMPTY <= SV_FREAD_IDLE and RXFIFO_EMPTY_IN;
----------------------------------------------------------------


--**********************************************************
-- FIFO READ STATE MACHINE
--**********************************************************
-- NOTE: RX FIFO MAY NOT BE READ AFTER DISABLING RX CHANNEL (ENA_RX USED TO RESET SM)
--FIFO READ STATE MACHINE SYNCHRONIZATION 
FREAD_currentStateProc: process(RST_I, CLK_I, ENA_RX ,CLR_RX_FIFO, FREAD_nextState) begin
      if (RST_I = '1' or CLR_RX_FIFO = '1' or ENA_RX = '0' ) then FREAD_currentState <= FREAD_IDLE;
       elsif (CLK_I'event and CLK_I = '1') then 
		FREAD_currentState <= FREAD_nextState;
      end if;
end process FREAD_currentStateProc;
----------------------------------------------------------------
--FIFO READ STATE MACHINE TRANSTIONS
FREAD_nextStateProc: process(FREAD_currentState,ENA_RX ,CLR_RX_FIFO,RXFIFO_EMPTY_IN,READ_PULSE_IN) 
begin
case FREAD_currentState is

when 	FREAD_IDLE 	=>
if RXFIFO_EMPTY_IN = '0' and CLR_RX_FIFO = '0' and ENA_RX = '1' then FREAD_nextState <=  FREAD_ENABLE;
end if;
when FREAD_ENABLE => FREAD_nextState <=  FREAD_LATCH ;

when FREAD_LATCH => FREAD_nextState <=  FREAD_HOLD ;

when FREAD_HOLD =>
if READ_PULSE_IN = '1' then FREAD_nextState <=  FREAD_IDLE;
end if;

when 	others => 		FREAD_nextState <=  FREAD_IDLE;
end case;
end process FREAD_nextStateProc;
----------------------------------------------------------------

--**********************************************************
-- RXFIFO_READ COMBINATIONAL LOGIC:
--**********************************************************
RXFIFO_READ_GEN: process (FREAD_currentState)
begin
case FREAD_currentState is
when 	FREAD_IDLE 		=>  	RXFIFO_READ <= '0';
when FREAD_ENABLE 	=> 	RXFIFO_READ <= '1';
when FREAD_LATCH		=>	RXFIFO_READ <= '0';
when FREAD_HOLD 		=>	RXFIFO_READ <= '0';
when 	others 		=>  	RXFIFO_READ <= '0';
end case;
end process RXFIFO_READ_GEN;
----------------------------------------------------------------

--**********************************************************
-- LATCH_DOUT COMBINATIONAL LOGIC:
--**********************************************************
LATCH_DOUT_GEN: process (FREAD_currentState)
begin
case FREAD_currentState is
when 	FREAD_IDLE 		=>  	LATCH_DOUT <= '0';
when FREAD_ENABLE 	=> 	LATCH_DOUT <= '0';
when FREAD_LATCH		=>	LATCH_DOUT <= '1';
when FREAD_HOLD 		=>	LATCH_DOUT <= '0';
when 	others 		=>  	LATCH_DOUT <= '0';
end case;
end process LATCH_DOUT_GEN;
----------------------------------------------------------------

--**********************************************************
-- SV_FREAD_IDLE COMBINATIONAL LOGIC:
--**********************************************************
SV_FREAD_IDLE_GEN: process (FREAD_currentState)
begin
case FREAD_currentState is
when 	FREAD_IDLE 		=>  	SV_FREAD_IDLE <= '1';
when FREAD_ENABLE 	=> 	SV_FREAD_IDLE <= '0';
when FREAD_LATCH		=>	SV_FREAD_IDLE <= '0';
when FREAD_HOLD 		=>	SV_FREAD_IDLE <= '0';
when 	others 		=>  	SV_FREAD_IDLE <= '0';
end case;
end process SV_FREAD_IDLE_GEN;

----------------------------------------------------------------


-- FIFO WRITE LOGIC:
--**********************************************************
-- FIFO WRITE STATE MACHINE
--**********************************************************
-- NOTE: RX FIFO CAN ONLY BE WRITTEN AFTER ENABLING RX CHANNEL (ENA_RX USED TO RESET SM)
--FIFO WRITE STATE MACHINE SYNCHRONIZATION 
FWRITE_currentStateProc: process(RST_I, CLK_I, ENA_RX ,CLR_RX_FIFO, FWRITE_nextState) begin
      if (RST_I = '1' or ENA_RX = '0' or CLR_RX_FIFO = '1' ) then FWRITE_currentState <= FWRITE_IDLE;
       elsif (CLK_I'event and CLK_I = '1') then 
		FWRITE_currentState <= FWRITE_nextState;
      end if;
end process FWRITE_currentStateProc;
----------------------------------------------------------------
--FIFO WRITE STATE MACHINE TRANSTIONS
FWRITE_nextStateProc: process(FWRITE_currentState,CLR_RX_FIFO,ENA_RX ,SL_RDV_IN ) 
begin
case FWRITE_currentState is

when 	FWRITE_IDLE 	=>
if ENA_RX = '1' and SL_RDV_IN = '1' and CLR_RX_FIFO = '0' then FWRITE_nextState <=  FWRITE_ENABLE;
end if;
when FWRITE_ENABLE => FWRITE_nextState <=  FWRITE_WACK;
 
when FWRITE_WACK =>
if SL_RDV_IN = '0' then FWRITE_nextState <=  FWRITE_IDLE;
end if;

when 	others => 		FWRITE_nextState <=  FWRITE_IDLE;
end case;
end process FWRITE_nextStateProc;
----------------------------------------------------------------


--**********************************************************
-- RXFIFO_WRITE COMBINATIONAL LOGIC:
--**********************************************************
RXFIFO_WRITE_GEN: process (FWRITE_currentState)
begin
case FWRITE_currentState is
when 	FWRITE_IDLE 		=>  	RXFIFO_WRITE <= '0';
when FWRITE_ENABLE 		=> 	RXFIFO_WRITE <= '1';
when FWRITE_WACK			=>	RXFIFO_WRITE <= '0';
when 	others 			=>  	RXFIFO_WRITE <= '0';
end case;
end process RXFIFO_WRITE_GEN;
----------------------------------------------------------------

--**********************************************************
-- RX_READ COMBINATIONAL LOGIC:
--**********************************************************
-- RX_READ IS THE HANDSHAKE SIGNAL BACK TO THE SL_RX_CHANNEL MODULE
-- INDICATING THAT THE DATA HAS BEEN WRITTEN TO THE RX FIFO
-- AND SL_RDV CAN NOW BE CLEARED (INSIDE SL_RX_CHANNEL)

RX_READ_GEN: process (FWRITE_currentState)
begin
case FWRITE_currentState is
when 	FWRITE_IDLE 		=>  	RX_READ <= '0';
when FWRITE_ENABLE 		=> 	RX_READ <= '0';
when FWRITE_WACK			=>	RX_READ <= '1';  --ASSERT AFTER DATA WRITTEN TO RX FIFO
when 	others 			=>  	RX_READ <= '0';
end case;
end process RX_READ_GEN;
----------------------------------------------------------------

end rtl;

