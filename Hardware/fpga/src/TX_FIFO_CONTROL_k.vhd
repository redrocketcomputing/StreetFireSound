----------------------------------------------------------------------------
--
--  File:   TX_FIFO_CONTROL_k.vhd
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
--
--	3-5-04 Synchronized intermodule signals:SL_XDV , TXFIFO_EMPTY, TX_READY 
--	Created 3-4-04 (modified from TX_FIFO_READER_k.vhd)
--	Added latch state to fifo read logic, 3-4-04
--
----------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;

library unisim;
use unisim.ALL;

entity TX_FIFO_CONTROL_k is
generic(BV_TX_ALMOST_EMPTY_CT: bit_vector (7 downto 0)); 
port ( 
	RST_I	:in std_logic; -- master reset for peripheral bus
	CLK_I	:in std_logic; -- master clock for peripheral bus
	ENA_TX  :in std_logic;  
	CLR_TX_FIFO : in std_logic; --3-4
	SL_XDV : in std_logic;      --3-4
	TXFIFO_WRITE: out std_logic; --3-4	
	TXFIFO_READ: out std_logic;
	TXFIFO_EMPTY: in std_logic;
	TXFIFO_FULL_OUT: in std_logic;
	SL_TDV: out std_logic;
	TX_READY: in std_logic;
	TXFIFO_ALMOST_EMPTY: out std_logic;
	TXFIFO_FULL: out std_logic;
	TXFIFO_DOUT:in std_logic_vector(7 downto 0);
	TXFIFO_COUNT:in std_logic_vector(7 downto 0);
	SLTX_CT: out std_logic_vector(7 downto 0)
	);
end TX_FIFO_CONTROL_k;

architecture rtl of TX_FIFO_CONTROL_k is

signal FIFO_DOUT_LATCH: std_logic_vector(7 downto 0);
signal LATCH_DOUT:std_logic;

signal TX_ALMOST_EMPTY_CT: std_logic_vector(7 downto 0);

type FREAD_StateType is (
FREAD_IDLE, 
FREAD_ENABLE,
FREAD_LATCH,
FREAD_DV,
FREAD_RACK);
signal FREAD_currentState, FREAD_nextState : FREAD_StateType;

type FWRITE_StateType is (
FWRITE_IDLE, 
FWRITE_ENABLE,
FWRITE_WACK);
signal FWRITE_currentState, FWRITE_nextState : FWRITE_StateType;

signal SL_XDV_IN , TXFIFO_EMPTY_IN, TX_READY_IN :std_logic;
--**********************************************************
--  LOGIC BEGINS HERE:
--**********************************************************
  
begin

-------------------------------------------------
--3-5-04 Added intermodule sync for SL_XDV , TXFIFO_EMPTY, TX_READY 
-------------------------------------------------
SL_XDV_IN_GEN: process (CLK_I,SL_XDV) 
begin 
if (CLK_I 'event and CLK_I = '1') then SL_XDV_IN <= SL_XDV;
end if;
end process SL_XDV_IN_GEN;
-------------------------------------------------
TXFIFO_EMPTY_IN_GEN: process (CLK_I,TXFIFO_EMPTY) 
begin 
if (CLK_I 'event and CLK_I = '1') then TXFIFO_EMPTY_IN <= TXFIFO_EMPTY;
end if;
end process TXFIFO_EMPTY_IN_GEN;
-------------------------------------------------
TX_READY_IN_GEN: process (CLK_I,TX_READY) 
begin 
if (CLK_I 'event and CLK_I = '1') then TX_READY_IN <= TX_READY;
end if;
end process TX_READY_IN_GEN;
-------------------------------------------------


--FIFO READ LOGIC:

--**********************************************************
-- FIFO_DOUT_LATCH:
--**********************************************************

SLTX_CT <= FIFO_DOUT_LATCH;

FIFO_DOUT_LATCH_GEN: process (CLK_I, RST_I, ENA_TX, LATCH_DOUT,TXFIFO_DOUT, FIFO_DOUT_LATCH)
begin

if (RST_I = '1' or ENA_TX = '0') then	
					FIFO_DOUT_LATCH <= (others => '0');
elsif (CLK_I'event and CLK_I = '1') then 
	if LATCH_DOUT = '1' then  
		FIFO_DOUT_LATCH <= TXFIFO_DOUT;
	else FIFO_DOUT_LATCH <= FIFO_DOUT_LATCH;
	end if;
end if;
end process FIFO_DOUT_LATCH_GEN;
----------------------------------------------------------------
--**********************************************************
-- TXFIFO_STATUS_DECODE: TXFIFO_FULL and TXFIFO_ALMOST_EMPTY 
--**********************************************************
TX_ALMOST_EMPTY_CT <= to_stdlogicvector(BV_TX_ALMOST_EMPTY_CT);

TXFIFO_ALMOST_EMPTY_GEN: process (TXFIFO_COUNT,TX_ALMOST_EMPTY_CT)
begin
if TXFIFO_COUNT > TX_ALMOST_EMPTY_CT then TXFIFO_ALMOST_EMPTY <= '0';
else TXFIFO_ALMOST_EMPTY <= '1';
end if;
end process TXFIFO_ALMOST_EMPTY_GEN;


TXFIFO_FULL <= TXFIFO_FULL_OUT; -- USE FIFO OUTPUT FOR NOW
----------------------------------------------------------------


--**********************************************************
-- FIFO READ STATE MACHINE
--**********************************************************
-- NOTE: TX FIFO WILL NOT BE READ PRIOR TO ENABLING TX CHANNEL (ENA_TX USED TO RESET SM)
--FIFO READ STATE MACHINE SYNCHRONIZATION 
FREAD_currentStateProc: process(RST_I, CLK_I, ENA_TX,CLR_TX_FIFO, FREAD_nextState) begin
      if (RST_I = '1' or ENA_TX = '0' or CLR_TX_FIFO = '1' ) then FREAD_currentState <= FREAD_IDLE;
       elsif (CLK_I'event and CLK_I = '1') then 
		FREAD_currentState <= FREAD_nextState;
      end if;
end process FREAD_currentStateProc;
----------------------------------------------------------------
--FIFO READ STATE MACHINE TRANSTIONS
FREAD_nextStateProc: process(FREAD_currentState,ENA_TX ,CLR_TX_FIFO , TXFIFO_EMPTY_IN, TX_READY_IN ) 
begin
case FREAD_currentState is

when 	FREAD_IDLE 	=>
if TXFIFO_EMPTY_IN = '0' and TX_READY_IN = '1' and ENA_TX = '1' and CLR_TX_FIFO = '0'
	then FREAD_nextState <=  FREAD_ENABLE;
end if;
when FREAD_ENABLE => FREAD_nextState <=  FREAD_LATCH;

when FREAD_LATCH => FREAD_nextState <=  FREAD_DV ;
 
when  FREAD_DV =>  FREAD_nextState <=  FREAD_RACK;

when FREAD_RACK =>
if TX_READY_IN = '0' then FREAD_nextState <=  FREAD_IDLE;
end if;

when 	others => 		FREAD_nextState <=  FREAD_IDLE;
end case;
end process FREAD_nextStateProc;
----------------------------------------------------------------


--**********************************************************
-- TXFIFO_READ COMBINATIONAL LOGIC:
--**********************************************************
TXFIFO_READ_GEN: process (FREAD_currentState)
begin
case FREAD_currentState is
when 	FREAD_IDLE 		=>  	TXFIFO_READ <= '0';
when FREAD_ENABLE 	=> 	TXFIFO_READ <= '1';
when FREAD_LATCH 		=>	TXFIFO_READ <= '0';
when FREAD_DV		=>	TXFIFO_READ <= '0';
when FREAD_RACK 		=>	TXFIFO_READ <= '0';
when 	others 		=>  	TXFIFO_READ <= '0';
end case;
end process TXFIFO_READ_GEN;
----------------------------------------------------------------

--**********************************************************
-- LATCH_DOUT COMBINATIONAL LOGIC:
--**********************************************************
LATCH_DOUT_GEN: process (FREAD_currentState)
begin
case FREAD_currentState is
when 	FREAD_IDLE 		=>  	LATCH_DOUT <= '0';
when FREAD_ENABLE 	=> 	LATCH_DOUT <= '0';
when FREAD_LATCH 		=>	LATCH_DOUT <= '1';
when FREAD_DV		=>	LATCH_DOUT <= '0';
when FREAD_RACK 		=>	LATCH_DOUT <= '0';
when 	others 		=>  	LATCH_DOUT <= '0';
end case;
end process LATCH_DOUT_GEN;
----------------------------------------------------------------

--**********************************************************
-- SL_TDV COMBINATIONAL LOGIC:
--**********************************************************
SL_TDV_GEN: process (FREAD_currentState)
begin
case FREAD_currentState is
when 	FREAD_IDLE 		=>  	SL_TDV <= '0';
when FREAD_ENABLE 	=> 	SL_TDV <= '0';
when FREAD_LATCH 		=>	SL_TDV <= '0';
when FREAD_DV		=>	SL_TDV <= '1';
when FREAD_RACK 		=>	SL_TDV <= '0';
when 	others 		=>  	SL_TDV <= '0';
end case;
end process SL_TDV_GEN;
----------------------------------------------------------------

-- FIFO WRITE LOGIC:
--**********************************************************
-- FIFO WRITE STATE MACHINE
--**********************************************************
-- NOTE: TX FIFO CAN BE WRITTEN PRIOR TO ENABLING TX CHANNEL (ENA_TX NOT USED TO RESET SM)
--FIFO WRITE STATE MACHINE SYNCHRONIZATION 
FWRITE_currentStateProc: process(RST_I, CLK_I, CLR_TX_FIFO, FWRITE_nextState) begin
      if (RST_I = '1' or  CLR_TX_FIFO = '1' ) then FWRITE_currentState <= FWRITE_IDLE;
       elsif (CLK_I'event and CLK_I = '1') then 
		FWRITE_currentState <= FWRITE_nextState;
      end if;
end process FWRITE_currentStateProc;
----------------------------------------------------------------
--FIFO WRITE STATE MACHINE TRANSTIONS
FWRITE_nextStateProc: process(FWRITE_currentState,CLR_TX_FIFO,SL_XDV_IN ) 
begin
case FWRITE_currentState is

when 	FWRITE_IDLE 	=>
if SL_XDV_IN = '1' and CLR_TX_FIFO = '0' then FWRITE_nextState <=  FWRITE_ENABLE;
end if;
when FWRITE_ENABLE => FWRITE_nextState <=  FWRITE_WACK;
 
when FWRITE_WACK =>
if SL_XDV_IN = '0' then FWRITE_nextState <=  FWRITE_IDLE;
end if;

when 	others => 		FWRITE_nextState <=  FWRITE_IDLE;
end case;
end process FWRITE_nextStateProc;
----------------------------------------------------------------


--**********************************************************
-- TXFIFO_WRITE COMBINATIONAL LOGIC:
--**********************************************************
TXFIFO_WRITE_GEN: process (FWRITE_currentState)
begin
case FWRITE_currentState is
when 	FWRITE_IDLE 		=>  	TXFIFO_WRITE <= '0';
when FWRITE_ENABLE 		=> 	TXFIFO_WRITE <= '1';
when FWRITE_WACK			=>	TXFIFO_WRITE <= '0';
when 	others 			=>  	TXFIFO_WRITE <= '0';
end case;
end process TXFIFO_WRITE_GEN;
----------------------------------------------------------------





end rtl;

