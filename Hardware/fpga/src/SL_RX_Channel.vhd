----------------------------------------------------------------------------
--
--  File:   SL_RX_Channel.vhd
--  Rev:    1.0.0
--  Date:	3-04-04
--  This is the VHDL module for the Kth SLINK Receiver for the SLINK Peripheral Module
-- in the Streetfire RBX Companion Chip (FPGA) for Streetfire Street Racer CPU Card to 
--  Application Board Interface
--
--  Author: Robyn E. Bauer
--
--	History: 
--	
--	Created 2-27-04 (modified from Receive_k_FIFO.vhd)
--	Added ENA_RX to next state process, 3-4-04
----------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;

library unisim;
use unisim.ALL;

entity SL_RX_Channel is 
	generic(BV_MIN_ECOUNT: bit_vector (7 downto 0)  ); 
	port ( 
	RST_I	:in std_logic; -- master reset for peripheral bus
	CLK_I	:in std_logic; -- master clock for peripheral bus
	PBEN_I  : in std_logic;
	SCLK	:in std_logic; -- SLINK CLock (timebase of 10us)
	ENA_RX :in std_logic; -- SLINK Enable from control register
	SL_RX	: in std_logic; -- SLINK RX signal
	SLRX_CT :out std_logic_vector(7 downto 0); -- Receive  Count (low time) 	
	SL_RDV	:out std_logic;
	RX_READ :in std_logic
);
end SL_RX_Channel;


architecture rtl of SL_RX_Channel is


-- SIGNAL DEFINITIONS:

signal RX_CTR: std_logic_vector (7 downto 0); -- Recieve Counter
signal INC_CNT,RES_CNT :std_logic; -- Transmit counter decrement and load signals

signal RX_CTR_LATCH:std_logic_vector (7 downto 0); -- Recieve Counter output latch

signal RCV_DV : std_logic; -- latched data valid output and clear input to SEND count latch

signal ECOUNT : std_logic_vector (7 downto 0); 

signal RCV_SL_out :std_logic_vector (7 downto 0); -- Recieve Counter Latch temp output
signal SL_RDV_out :std_logic; -- temp signal for Latched Data valid output

-- SL Receive State Machine

type SLRX_StateType is (
RX_IDLE, 
RESET_SCNT,
S_WAIT_LOW,
S_WAIT_HIGH,
INCR_SCNT,
SAVE_SCNT,
D_WAIT_LOW,
D_WAIT_HIGH,
INCR_DCNT,
RX_END1,
RX_END2);

signal SLRX_currentState, SLRX_nextState : SLRX_StateType;

signal END_MIN_EXCEEDED: std_logic;  -- combinational flag for end count exceeded 

signal SSCLK,SCLK_D1:std_logic;  -- Synchronized SCLK
signal SSL_RX,SL_RX_D1 :std_logic; -- synch'd SL_RX input

signal RX_READ_in:std_logic; --sync'd read pulse input

signal LATCH_EN:std_logic;
--**********************************************************
--  LOGIC BEGINS HERE:
--**********************************************************
  
begin

--**********************************************************
-- SLINK RX STATE MACHINE
--**********************************************************
--SLINK RX STATE MACHINE SYNCHRONIZATION 
SLRX_currentStateProc: process(RST_I, CLK_I, PBEN_I,SLRX_nextState,ENA_RX) begin
      if (RST_I = '1' or PBEN_I = '0' or ENA_RX = '0') then SLRX_currentState <= RX_IDLE;
       elsif (CLK_I'event and CLK_I = '1') then 
		SLRX_currentState <= SLRX_nextState;
      end if;
end process SLRX_currentStateProc;

-- SLRX STATE MACHINE TRANSTIONS
SLRX_nextStateProc: process(SLRX_currentState,ENA_RX ,SSCLK,SSL_RX,END_MIN_EXCEEDED) 
begin
case SLRX_currentState is
when RX_IDLE 		=>
	if ENA_RX ='1' and SSL_RX = '0' then SLRX_nextState <= RESET_SCNT;
	end if;
when RESET_SCNT 	=>  SLRX_nextState <= S_WAIT_LOW;
when S_WAIT_LOW 	=>
	if SSL_RX = '1' then SLRX_nextState <= SAVE_SCNT;
	elsif SSCLK = '0' then SLRX_nextState <= S_WAIT_HIGH;
	end if;
when S_WAIT_HIGH 	=>
	if SSL_RX = '1' then SLRX_nextState <= SAVE_SCNT;
	elsif SSCLK = '1' then SLRX_nextState <= INCR_SCNT;
	end if;
when INCR_SCNT 		=>  SLRX_nextState <= S_WAIT_LOW;
when SAVE_SCNT 		=>  SLRX_nextState <= D_WAIT_LOW;
when D_WAIT_LOW 	=>
	if SSL_RX = '0' then SLRX_nextState <= RESET_SCNT;
	elsif SSCLK = '0' then  SLRX_nextState <= D_WAIT_HIGH;
	end if;
when D_WAIT_HIGH 	=>
	if SSL_RX = '0' then SLRX_nextState <= RESET_SCNT;
	elsif SSCLK = '1' and END_MIN_EXCEEDED = '0' then SLRX_nextState <= INCR_DCNT;
	elsif SSCLK = '1' and END_MIN_EXCEEDED = '1' then SLRX_nextState <= RX_END1;
	end if;
when INCR_DCNT 		=>  SLRX_nextState <= D_WAIT_LOW;
when RX_END1 		=>  SLRX_nextState <= RX_END2;
when RX_END2 		=>  SLRX_nextState <= RX_IDLE;
when others		=>  SLRX_nextState <= RX_IDLE;
end case;
end process SLRX_nextStateProc;
-- END SLINK RX STATE MACHINE
--**********************************************************
----------------------------------------------------------------
--**********************************************************
-- SCLK Synch LOGIC:
--**********************************************************
SCLK_SYNCH: process (CLK_I, RST_I, PBEN_I, ENA_RX, SCLK,SCLK_D1,SSCLK)
begin
if (RST_I = '1' or PBEN_I = '0' or ENA_RX = '0') then 
			SCLK_D1 <= '0';
			SSCLK <= '0';
elsif (CLK_I'event and CLK_I = '1') then 
			SCLK_D1 <= SCLK;
			SSCLK <= SCLK_D1;
end if;
end process SCLK_SYNCH;
----------------------------------------------------------------

--**********************************************************
-- SL_RX Synch LOGIC:
--**********************************************************
SL_RX_SYNCH: process (CLK_I, RST_I,PBEN_I, ENA_RX, SL_RX,SL_RX_D1,SSL_RX)
begin
if (RST_I = '1' or PBEN_I = '0' or ENA_RX = '0') then 
			SL_RX_D1 <= '1';
			SSL_RX <= '1';
elsif (CLK_I'event and CLK_I = '1') then 
			SL_RX_D1 <= SL_RX;
			SSL_RX <= SL_RX_D1;
end if;
end process SL_RX_SYNCH;
----------------------------------------------------------------

--**********************************************************
-- RX COUNTER:
--**********************************************************
RX_COUNTER: process (CLK_I, RST_I, PBEN_I, ENA_RX, INC_CNT,RES_CNT ,RX_CTR )
begin

if (RST_I = '1' or PBEN_I = '0' or ENA_RX = '0') then	
				RX_CTR <= (others => '0');
elsif (CLK_I'event and CLK_I = '1') then 
	if RES_CNT = '1' then RX_CTR <= (others => '0');
	elsif INC_CNT = '1'  then RX_CTR <= RX_CTR + 1 ;
	end if;
end if;
end process RX_COUNTER;
----------------------------------------------------------------



--**********************************************************
-- RX Counter Output LATCH:
--**********************************************************
RX_CTR_LATCH_GEN: process (CLK_I, RST_I, PBEN_I, ENA_RX, RCV_DV,RX_CTR, RX_CTR_LATCH)
begin

if (RST_I = '1' or PBEN_I = '0' or ENA_RX = '0') then	
					RX_CTR_LATCH <= (others => '0');
elsif (CLK_I'event and CLK_I = '1') then 
	if RCV_DV = '1' then  
					RX_CTR_LATCH <= RX_CTR;
	else RX_CTR_LATCH <= RX_CTR_LATCH;
	end if;
end if;
end process RX_CTR_LATCH_GEN;
----------------------------------------------------------------

--**********************************************************
-- LATCH_EN LOGIC: one clock delay from RCV_DV
--**********************************************************
LATCH_EN_GEN: process (RST_I,CLK_I,RCV_DV)
begin
if (RST_I = '1') then	LATCH_EN <= '0';
elsif (CLK_I'event and CLK_I = '1') then LATCH_EN <= RCV_DV;
end if;
end process LATCH_EN_GEN;
----------------------------------------------------------------


--**********************************************************
-- RECEIVE COUNT OUTPUT REGISTER:
--**********************************************************
RCV_SL_LATCH: process (CLK_I, RST_I, PBEN_I, LATCH_EN,RX_CTR_LATCH, RCV_SL_out)
begin

if (RST_I = '1' or PBEN_I = '0') then RCV_SL_out <= (others => '0');
elsif (CLK_I'event and CLK_I = '1') then 
	if LATCH_EN = '1' then  RCV_SL_out <= RX_CTR_LATCH;
	else 			RCV_SL_out <= RCV_SL_out;
	end if;
end if;
end process RCV_SL_LATCH;
----------------------------------------------------------------
SLRX_CT <= RCV_SL_out;

--**********************************************************
-- INC_CNT COMBINATIONAL LOGIC:
--**********************************************************
INC_CNT_GEN: process (SLRX_currentState)
begin
case SLRX_currentState is
when RX_IDLE 		=>  INC_CNT <= '0';
when RESET_SCNT 	=>  INC_CNT <= '0';
when S_WAIT_LOW 	=>  INC_CNT <= '0';
when S_WAIT_HIGH 	=>  INC_CNT <= '0';
when INCR_SCNT 		=>  INC_CNT <= '1';
when SAVE_SCNT 		=>  INC_CNT <= '0';
when D_WAIT_LOW 	=>  INC_CNT <= '0';
when D_WAIT_HIGH 	=>  INC_CNT <= '0';
when INCR_DCNT 		=>  INC_CNT <= '1';
when RX_END1 		=>  INC_CNT <= '0';
when RX_END2 		=>  INC_CNT <= '0';
when others		=>  INC_CNT <= '0';
end case;
end process INC_CNT_GEN;
----------------------------------------------------------------

--**********************************************************
-- RES_CNT COMBINATIONAL LOGIC:
--**********************************************************
RES_CNT_GEN: process (SLRX_currentState)
begin
case SLRX_currentState is
when RX_IDLE 		=>  RES_CNT <= '0';
when RESET_SCNT 	=>  RES_CNT <= '1';
when S_WAIT_LOW 	=>  RES_CNT <= '0';
when S_WAIT_HIGH 	=>  RES_CNT <= '0';
when INCR_SCNT 		=>  RES_CNT <= '0';
when SAVE_SCNT 		=>  RES_CNT <= '1';
when D_WAIT_LOW 	=>  RES_CNT <= '0';
when D_WAIT_HIGH 	=>  RES_CNT <= '0';
when INCR_DCNT 		=>  RES_CNT <= '0';
when RX_END1 		=>  RES_CNT <= '1';
when RX_END2 		=>  RES_CNT <= '0';
when others		=>  RES_CNT <= '0';
end case;
end process RES_CNT_GEN;
----------------------------------------------------------------

--**********************************************************
-- RCV_DV COMBINATIONAL LOGIC:
--**********************************************************
RCV_DV_GEN: process (SLRX_currentState)
begin
case SLRX_currentState is
when RX_IDLE 		=>  RCV_DV <= '0';
when RESET_SCNT 	=>  RCV_DV <= '0';
when S_WAIT_LOW 	=>  RCV_DV <= '0';
when S_WAIT_HIGH 	=>  RCV_DV <= '0';
when INCR_SCNT 		=>  RCV_DV <= '0';
when SAVE_SCNT 		=>  RCV_DV <= '1';
when D_WAIT_LOW 	=>  RCV_DV <= '0';
when D_WAIT_HIGH 	=>  RCV_DV <= '0';
when INCR_DCNT 		=>  RCV_DV <= '0';
when RX_END1 		=>  RCV_DV <= '0';
when RX_END2 		=>  RCV_DV <= '1';
when others		=>  RCV_DV <= '0';
end case;
end process RCV_DV_GEN;
----------------------------------------------------------------

ECOUNT <= to_stdlogicvector(BV_MIN_ECOUNT);

--**********************************************************
-- END_MIN_EXCEEDED LOGIC:
--**********************************************************
END_MIN_EXCEEDED_LOGIC: process (ECOUNT,RX_CTR)
begin
if (RX_CTR > ECOUNT) then END_MIN_EXCEEDED <= '1';	
else 	END_MIN_EXCEEDED <= '0';				
end if;
end process END_MIN_EXCEEDED_LOGIC;
----------------------------------------------------------------

--**********************************************************
-- RX_READ Synch LOGIC:
--**********************************************************
RX_READ_SYNCH: process (CLK_I, RST_I, PBEN_I, ENA_RX, RX_READ)
begin
if (RST_I = '1' or PBEN_I = '0' or ENA_RX = '0') then 
			RX_READ_in <= '0';
elsif (CLK_I'event and CLK_I = '1') then 
			RX_READ_in <= RX_READ;
end if;
end process RX_READ_SYNCH;
----------------------------------------------------------------

--**********************************************************
-- SL_RDV (latched data valid) OUTPUT LOGIC:
--**********************************************************
-- set on LATCH_EN = '1', CLEAR ON RX_READ_in = '1'
SL_RDV_out_GEN: process (RST_I,CLK_I,PBEN_I,LATCH_EN,RX_READ_in )
begin
 if (RST_I = '1' or PBEN_I = '0' ) then SL_RDV_out <= '0';
 elsif (CLK_I'event and CLK_I = '1') then 
	if LATCH_EN = '1' then SL_RDV_out <= '1';
	elsif RX_READ_in = '1' then SL_RDV_out <= '0';
	end if;
 end if;
end process SL_RDV_out_GEN;
----------------------------------------------------------------
SL_RDV <= SL_RDV_out;

end rtl;

