----------------------------------------------------------------------------
--
--  File:   SL_Line_Status.vhd
--  Rev:    1.0.0
--  Date:	3-5-04
--  This is the VHDL module for the Kth SLINK Line Status Module for the SLINK Peripheral Module
-- in the Streetfire RBX Companion Chip (FPGA) for Streetfire Street Racer CPU Card to 
--  Application Board Interface
--
--  Author: Robyn E. Bauer
--
--	History: 
--	
--	Created 2-27-04 (modified from Receive_k_FIFO.vhd)
----------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;

library unisim;
use unisim.ALL;

entity SL_Line_Status is 
	generic(BV_IDLE_COUNT: bit_vector (7 downto 0)  ); --Idle count in CLK_I's
	port ( 
	RST_I	:in std_logic; -- master reset for peripheral bus
	CLK_I	:in std_logic; -- master clock for peripheral bus
	PBEN_I  : in std_logic;
	SCLK	:in std_logic; -- SLINK CLock (timebase of 10us)
	SL_RX	: in std_logic; -- SLINK RX signal
	INT_LI	:out std_logic  -- INDICATES LINE IS IDLE (AFTER 3 DELIMITER TIMES)
);
end SL_Line_Status;


architecture rtl of SL_Line_Status is


-- SIGNAL DEFINITIONS:

signal IDLE_CTR: std_logic_vector (7 downto 0); -- LINE IDLE Counter
signal INC_CNT,RES_CNT :std_logic; -- Transmit counter decrement and load signals

signal ICOUNT : std_logic_vector (7 downto 0); 

signal INT_LI_out: std_logic;
-- SL Receive State Machine

type SLIDLE_StateType is (
SL_IDLE, 
SL_LOW,
SL_LOAD_ICOUNT,
SL_HIGH);

signal SLIDLE_currentState, SLIDLE_nextState : SLIDLE_StateType;


signal ICOUNT_EXCEEDED: std_logic;  -- combinational flag for end count exceeded 

signal SSCLK,SCLK_D1:std_logic;  -- Synchronized SCLK
signal SSL_RX,SL_RX_D1 :std_logic; -- synch'd SL_RX input



--**********************************************************
--  LOGIC BEGINS HERE:
--**********************************************************
  
begin

ICOUNT <= to_stdlogicvector(BV_IDLE_COUNT);


--**********************************************************
-- SLINK IDLE STATE MACHINE
--**********************************************************

SLIDLE_currentStateProc: process(RST_I, CLK_I, PBEN_I,SLIDLE_nextState) begin
      if (RST_I = '1' or PBEN_I = '0') then SLIDLE_currentState <= SL_IDLE;
       elsif (CLK_I'event and CLK_I = '1') then 
		SLIDLE_currentState <= SLIDLE_nextState;
      end if;
end process SLIDLE_currentStateProc;

-- SLIDLE STATE MACHINE TRANSTIONS
SLIDLE_nextStateProc: process(SLIDLE_currentState,PBEN_I ,SSL_RX,ICOUNT_EXCEEDED) 
begin
case SLIDLE_currentState is

when SL_IDLE => 
	if SSL_RX = '0'and PBEN_I = '1' then SLIDLE_nextState <= SL_LOW;
	end if;
when SL_LOW => 
	if SSL_RX = '1' then SLIDLE_nextState <= SL_LOAD_ICOUNT;
	end if;
when SL_LOAD_ICOUNT => SLIDLE_nextState <= SL_HIGH;
when SL_HIGH => 
	if SSL_RX = '0' then SLIDLE_nextState <= SL_LOW;
	elsif ICOUNT_EXCEEDED = '1' then SLIDLE_nextState <= SL_IDLE;
	end if;
when others =>  SLIDLE_nextState <= SL_IDLE;

end case;
end process SLIDLE_nextStateProc;
-- END SLINK RX STATE MACHINE
--**********************************************************
----------------------------------------------------------------
--**********************************************************
-- SCLK Synch LOGIC:
--**********************************************************
SCLK_SYNCH: process (CLK_I, RST_I, PBEN_I, SCLK,SCLK_D1,SSCLK)
begin
if (RST_I = '1' or PBEN_I = '0' ) then 
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
SL_RX_SYNCH: process (CLK_I, RST_I,PBEN_I,  SL_RX,SL_RX_D1,SSL_RX)
begin
if (RST_I = '1' or PBEN_I = '0' ) then 
			SL_RX_D1 <= '1';
			SSL_RX <= '1';
elsif (CLK_I'event and CLK_I = '1') then 
			SL_RX_D1 <= SL_RX;
			SSL_RX <= SL_RX_D1;
end if;
end process SL_RX_SYNCH;
----------------------------------------------------------------

--**********************************************************
-- IDLE COUNTER:
--**********************************************************
IDLE_COUNTER: process (CLK_I, RST_I, PBEN_I, SCLK_D1, SSCLK, INC_CNT,RES_CNT ,IDLE_CTR )
begin

if (RST_I = '1' or PBEN_I = '0' ) then	
				IDLE_CTR <= (others => '0');
elsif (CLK_I'event and CLK_I = '1') then 
	if RES_CNT = '1' then IDLE_CTR <= (others => '0');
	elsif SCLK_D1 = '0' and SSCLK ='1' and INC_CNT = '1'  then IDLE_CTR <= IDLE_CTR + 1 ;
	end if;
end if;
end process IDLE_COUNTER;
----------------------------------------------------------------

--**********************************************************
-- INC_CNT COMBINATIONAL LOGIC:
--**********************************************************
INC_CNT_GEN: process (SLIDLE_currentState)
begin
case SLIDLE_currentState is
when SL_IDLE 		=> INC_CNT <= '0';	
when SL_LOW 		=> INC_CNT <= '0';	
when SL_LOAD_ICOUNT 	=> INC_CNT <= '0';
when SL_HIGH 		=> INC_CNT <= '1'; 
when others		=> INC_CNT <= '0';
end case;
end process INC_CNT_GEN;
----------------------------------------------------------------

--**********************************************************
-- RES_CNT COMBINATIONAL LOGIC:
--**********************************************************
RES_CNT_GEN: process (SLIDLE_currentState)
begin
case SLIDLE_currentState is
when SL_IDLE 		=>  RES_CNT <= '0';
when SL_LOW 		=>  RES_CNT <= '0';
when SL_LOAD_ICOUNT 	=>  RES_CNT <= '1';
when SL_HIGH 		=>  RES_CNT <= '0';
when others		=>  RES_CNT <= '0';
end case;
end process RES_CNT_GEN;
----------------------------------------------------------------

--**********************************************************
-- ICOUNT_EXCEEDED LOGIC:
--**********************************************************
ICOUNT_EXCEEDED_LOGIC: process (ICOUNT,IDLE_CTR)
begin
if (IDLE_CTR > ICOUNT) then ICOUNT_EXCEEDED <= '1';	
else 	ICOUNT_EXCEEDED <= '0';				
end if;
end process ICOUNT_EXCEEDED_LOGIC;
----------------------------------------------------------------


--**********************************************************
-- INT_LI_out COMBINATIONAL LOGIC:
--**********************************************************
INT_LI_out_GEN: process (SLIDLE_currentState)
begin
case SLIDLE_currentState is
when SL_IDLE 		=>  INT_LI_out <= '1';
when SL_LOW 		=>  INT_LI_out <= '0';
when SL_LOAD_ICOUNT 	=>  INT_LI_out <= '0';
when SL_HIGH 		=>  INT_LI_out <= '0';
when others			=>  INT_LI_out <= '0';
end case;
end process INT_LI_out_GEN;
----------------------------------------------------------------
--**********************************************************
-- INT_LI output LOGIC:
--**********************************************************
INT_LI_GEN: process (CLK_I, RST_I, PBEN_I, INT_LI_out)
begin
if (RST_I = '1' or PBEN_I = '0' ) then INT_LI <= '0';
elsif (CLK_I'event and CLK_I = '1') then INT_LI<= INT_LI_out;
end if;
end process INT_LI_GEN;
----------------------------------------------------------------

end rtl;

