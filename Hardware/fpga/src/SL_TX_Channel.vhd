----------------------------------------------------------------------------
--
--  File:   SL_TX_Channel.vhd
--  Rev:    1.0.0
--  Date:	3-4-04
--  This is the VHDL module for the Kth SLINK Transmitter for the SLINK Peripheral Module
-- in the Streetfire RBX Companion Chip (FPGA) for Streetfire Street Racer CPU Card to 
--  Application Board Interface.  
--
--  Author: Robyn E. Bauer
--
--  Restrictions: 
--  1.  Do not use send count of less than 2, except for end of message count of 0
--  2.  Do not issue an initial send count of 0 (or 1).
--	
--
--	History: 
--	Added ENA_TX to next state process, 3-4-04
--    Modified TX_READY output to stay high whenever ready for new data, 3-1-04, reb
--	Created 2-27-04 Modified from Transmit_k_FIFO.vhd
----------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;

library unisim;
use unisim.ALL;

entity SL_TX_Channel is
	generic(BV_DCOUNT,BV_ECOUNT: bit_vector (7 downto 0)); 
	port ( 
	RST_I	:in std_logic; -- master reset for peripheral bus
	CLK_I	:in std_logic; -- master clock for peripheral bus
	PBEN_I  :in std_logic;
	SCLK	:in std_logic; -- SLINK CLock (timebase of 10us)
	ENA_TX	:in std_logic; -- SLINK Enable from control register
	SL_TDV	:in std_logic; -- SLINK Transmit data valid (count ready to transmit)
	SLTX_CT :in std_logic_vector(7 downto 0); -- Transmit Send Count (low time) from reg.
	TX_READY :out std_logic; -- indicates NEXT tx value CAN be WRITTEN TO register
	SL_TX	:out std_logic; -- SLINK TX signal
	SL_RX 	: in std_logic; -- SLINK RX input
	SL_TC	:out std_logic -- Transmit Collision Detected 
	);
end SL_TX_Channel;


architecture rtl of SL_TX_Channel is


signal SEND_CT : std_logic_vector(7 downto 0); -- Latched Version of SEND_SL (input from register)
signal TX_CTR: std_logic_vector (7 downto 0); -- Transmit Counter
signal DEC_CNT,LOAD_CNT :std_logic; -- Transmit counter decrement and load signals
signal ST_CNT :std_logic_vector(7 downto 0); -- Count value from State dependent logic


signal LXDV, CLR_DV : std_logic; -- latched data valid output and clear input to SEND count latch

signal DCOUNT, ECOUNT : std_logic_vector (7 downto 0); 

signal SL_TX_out,TX_READY_out,SL_TC_out : std_logic;  -- temporary holder for syncronous output

signal SSL_RX,SL_RX_D1: std_logic;  --synched version of SL_RX 

signal SL_TX_D1,SL_TX_D2,SL_TX_D3,SL_TX_D4,SL_TX_D5: std_logic;  --delayed version of TX for collision detect

-- SL Transmit State Machine

type SLTX_StateType is (
TX_IDLE, 
START_LO_WAIT,
START_HI_WAIT,
LOAD_SEND_CT,
SEND_LO_WAIT,
SEND_HI_WAIT,
DEC_SEND_CT,
LOAD_DEL_CT,
DEL_LO_WAIT,
DEL_HI_WAIT,
DEC_DEL_CT,
CLR_ZEROCT_DV,
LOAD_END_CT,
END_LO_WAIT,
END_HI_WAIT,
DEC_END_CT,
END_XMIT);

signal SLTX_currentState, SLTX_nextState : SLTX_StateType;

signal ZERO_COUNT:std_logic;
signal TXCTR_1,TXCTR_2:std_logic;

signal SL_TDV_in:std_logic;
signal SSCLK:std_logic;

--**********************************************************
--  LOGIC BEGINS HERE:
--**********************************************************
  
begin

--**********************************************************
-- SCLK Synch LOGIC:
--**********************************************************
SCLK_SYNCH: process (CLK_I, RST_I, PBEN_I, ENA_TX, SCLK,SSCLK)
begin
if (RST_I = '1' or PBEN_I = '0' or ENA_TX = '0') then 
			SSCLK <= '0';
elsif (CLK_I'event and CLK_I = '1') then 
			SSCLK <= SCLK;
end if;
end process SCLK_SYNCH;
----------------------------------------------------------------

--**********************************************************
-- SL_TDV input synch
--**********************************************************
SL_TDV_SYNCH: process(RST_I, CLK_I,PBEN_I, ENA_TX, SL_TDV) begin
      if (RST_I = '1' or PBEN_I ='0' or ENA_TX = '0') then SL_TDV_in <= '0';
       elsif (CLK_I'event and CLK_I = '1') then 
		SL_TDV_in <= SL_TDV;
      end if;
end process SL_TDV_SYNCH;

------------------------------------------------------------

--**********************************************************
-- SLINK TX STATE MACHINE
--**********************************************************
--SLINK TX STATE MACHINE SYNCHRONIZATION 
SLTX_currentStateProc: process(RST_I, CLK_I, PBEN_I,SLTX_nextState,ENA_TX) begin
      if (RST_I = '1' or PBEN_I = '0' or ENA_TX = '0') then SLTX_currentState <= TX_IDLE;
       elsif (CLK_I'event and CLK_I = '1') then 
		SLTX_currentState <= SLTX_nextState;
      end if;
end process SLTX_currentStateProc;

-- SLTX STATE MACHINE TRANSTIONS

SLTX_nextStateProc: process(SLTX_currentState,ENA_TX ,SSCLK,LXDV,TXCTR_1,TXCTR_2,ZERO_COUNT ) 
begin
case SLTX_currentState is

when 	TX_IDLE 	=>
	if ENA_TX = '1' and LXDV = '1' then SLTX_nextState <=  START_LO_WAIT ;
	end if;
when	START_LO_WAIT	=>
	if SSCLK = '0' then SLTX_nextState <=  START_HI_WAIT;
	end if;

when	START_HI_WAIT	=>
	if SSCLK = '1' and ZERO_COUNT = '1' then SLTX_nextState <= CLR_ZEROCT_DV;
	elsif SSCLK = '1' then SLTX_nextState <=  LOAD_SEND_CT;  
	end if;
when	LOAD_SEND_CT	=> SLTX_nextState <=  SEND_LO_WAIT;
when	SEND_LO_WAIT	=>
	if SSCLK = '0' then SLTX_nextState <=  SEND_HI_WAIT;
	end if;
when	SEND_HI_WAIT	=>
	if SSCLK = '1' and TXCTR_1 = '1' then SLTX_nextState <=  LOAD_DEL_CT;
	elsif SSCLK = '1' then SLTX_nextState <=  DEC_SEND_CT;
	end if;
when	DEC_SEND_CT	=> SLTX_nextState <=  SEND_LO_WAIT;	
when	LOAD_DEL_CT	=> SLTX_nextState <=  DEL_LO_WAIT;
when	DEL_LO_WAIT	=>
	if SSCLK = '0' then SLTX_nextState <=  DEL_HI_WAIT;
	end if;
when	DEL_HI_WAIT	=>
	if SSCLK = '1' and TXCTR_2 = '1' and LXDV = '0'  then SLTX_nextState <=  LOAD_END_CT; 
	elsif SSCLK = '1' and TXCTR_2 = '1' then SLTX_nextState <=  START_LO_WAIT ; -- start another low period
	elsif SSCLK = '1' then SLTX_nextState <=  DEC_DEL_CT; -- not at end of delimiter yet
	end if;
when	DEC_DEL_CT	=> SLTX_nextState <=  DEL_LO_WAIT;

when  CLR_ZEROCT_DV =>  SLTX_nextState <=  LOAD_END_CT;

when	LOAD_END_CT	=> SLTX_nextState <=  END_LO_WAIT;
when	END_LO_WAIT	=>
	if SSCLK = '0' then SLTX_nextState <=  END_HI_WAIT;
	end if;
when	END_HI_WAIT	=>
	if SSCLK = '1' and TXCTR_1 = '1' then SLTX_nextState <=  END_XMIT;
	elsif SSCLK = '1' then SLTX_nextState <=  DEC_END_CT;
	end if;
when	DEC_END_CT	=> SLTX_nextState <=  END_LO_WAIT;
when	END_XMIT	=> SLTX_nextState <=  TX_IDLE;
when 	others 		=> SLTX_nextState <=  TX_IDLE;
end case;
end process SLTX_nextStateProc;

--**********************************************************
-- END SLINK TX STATE MACHINE
--**********************************************************
----------------------------------------------------------------
--**********************************************************
-- ZERO COUNT DETECT:
--**********************************************************
ZERO_COUNT_LOGIC: process (CLK_I, RST_I, PBEN_I, ENA_TX, SEND_CT)
begin

if (RST_I = '1' or PBEN_I = '0' or ENA_TX = '0') then	
				ZERO_COUNT <= '0';
elsif (CLK_I'event and CLK_I = '1') then 
	if SEND_CT = x"00" then ZERO_COUNT <= '1';
	else ZERO_COUNT <= '0';
	end if;
end if;
end process ZERO_COUNT_LOGIC;


--*********************************************************
-- SEND COUNT LATCH:
--**********************************************************
SEND_CT_LATCH: process (CLK_I, RST_I,PBEN_I, ENA_TX, SL_TDV_in, CLR_DV, SLTX_CT )
begin

if (RST_I = '1' or PBEN_I = '0' or  ENA_TX = '0') then	
				LXDV <= '0';
				SEND_CT <= (others => '0');
elsif (CLK_I'event and CLK_I = '1') then 
	if SL_TDV_in = '1' then  
				LXDV <= '1';  -- indicates new data written
				SEND_CT <= SLTX_CT; -- external data input
	elsif CLR_DV = '1'  then
				LXDV <= '0';
				-- leave SEND_CT alone on clear
	end if;
end if;
end process SEND_CT_LATCH;
----------------------------------------------------------------


--**********************************************************
-- TX COUNTER:
--**********************************************************
TX_COUNTER: process (CLK_I, RST_I, PBEN_I, ENA_TX, ST_CNT, DEC_CNT, LOAD_CNT,TX_CTR )
begin

if (RST_I = '1' or PBEN_I = '0' or ENA_TX = '0') then	
				TX_CTR <= (others => '0');
elsif (CLK_I'event and CLK_I = '1') then 
	if LOAD_CNT= '1' then  
				TX_CTR <= ST_CNT;
	elsif DEC_CNT = '1'  then
				TX_CTR <= TX_CTR - 1 ;
	end if;
end if;
end process TX_COUNTER;
----------------------------------------------------------------

TXCTR_1_LOGIC: process (CLK_I, RST_I, PBEN_I, ENA_TX, TX_CTR)
begin

if (RST_I = '1' or PBEN_I = '0' or ENA_TX = '0') then	
				TXCTR_1 <= '0';
elsif (CLK_I'event and CLK_I = '1') then 
	if TX_CTR= x"01" then TXCTR_1 <= '1';
	else TXCTR_1<= '0';
	end if;
end if;
end process TXCTR_1_LOGIC;

TXCTR_2_LOGIC: process (CLK_I, RST_I, PBEN_I, ENA_TX, TX_CTR)
begin

if (RST_I = '1' or PBEN_I = '0' or ENA_TX = '0') then	
				TXCTR_2 <= '0';
elsif (CLK_I'event and CLK_I = '1') then 
	if TX_CTR= x"02" then TXCTR_2 <= '1';
	else TXCTR_2<= '0';
	end if;
end if;
end process TXCTR_2_LOGIC;

--**********************************************************
-- ST_CNT COMBINATIONAL LOGIC:
--**********************************************************

ECOUNT <= to_stdlogicvector(BV_ECOUNT);
DCOUNT <= to_stdlogicvector(BV_DCOUNT);


ST_CNT_GEN: process (SLTX_currentState,SEND_CT,ECOUNT,DCOUNT)
begin
case SLTX_currentState is

when 	TX_IDLE 	=>  	ST_CNT <= SEND_CT;
when	START_LO_WAIT	=>  	ST_CNT <= SEND_CT;
when	START_HI_WAIT	=>	ST_CNT <= SEND_CT;
when	LOAD_SEND_CT	=>  	ST_CNT <= SEND_CT;
when	SEND_LO_WAIT	=> 	ST_CNT <= SEND_CT;
when	SEND_HI_WAIT	=>	ST_CNT <= DCOUNT;
when	DEC_SEND_CT	=>	ST_CNT <= DCOUNT;
when	LOAD_DEL_CT	=>	ST_CNT <= DCOUNT;
when	DEL_LO_WAIT	=>	ST_CNT <= DCOUNT;
when	DEL_HI_WAIT	=>  	ST_CNT <= ECOUNT;
when	DEC_DEL_CT	=>  	ST_CNT <= ECOUNT;
when  CLR_ZEROCT_DV =>  ST_CNT <= ECOUNT;
when	LOAD_END_CT	=>  	ST_CNT <= ECOUNT;
when	END_LO_WAIT	=>  	ST_CNT <= ECOUNT;
when	END_HI_WAIT	=>  	ST_CNT <= ECOUNT;
when	DEC_END_CT	=>  	ST_CNT <= ECOUNT;
when	END_XMIT	=>  	ST_CNT <= SEND_CT;
when 	others 		=>  	ST_CNT <= SEND_CT;
end case;
end process ST_CNT_GEN;
----------------------------------------------------------------

--**********************************************************
-- DEC_CNT COMBINATIONAL LOGIC:
--**********************************************************
DEC_CNT_GEN: process (SLTX_currentState)
begin
case SLTX_currentState is

when 	TX_IDLE 	=>  	DEC_CNT <= '0';
when	START_LO_WAIT	=>  	DEC_CNT <= '0';
when	START_HI_WAIT	=>	DEC_CNT <= '0';
when	LOAD_SEND_CT	=>  	DEC_CNT <= '0';
when	SEND_LO_WAIT	=> 	DEC_CNT <= '0';
when	SEND_HI_WAIT	=>	DEC_CNT <= '0';
when	DEC_SEND_CT	=>	DEC_CNT <= '1';
when	LOAD_DEL_CT	=>	DEC_CNT <= '0';
when	DEL_LO_WAIT	=>	DEC_CNT <= '0';
when	DEL_HI_WAIT	=>  	DEC_CNT <= '0';
when	DEC_DEL_CT	=>  	DEC_CNT <= '1';
when  CLR_ZEROCT_DV =>  DEC_CNT <= '0';
when	LOAD_END_CT	=>  	DEC_CNT <= '0';
when	END_LO_WAIT	=>  	DEC_CNT <= '0';
when	END_HI_WAIT	=>  	DEC_CNT <= '0';
when	DEC_END_CT	=>  	DEC_CNT <= '1';
when	END_XMIT	=>  	DEC_CNT <= '0';
when 	others 		=>  	DEC_CNT <= '0';
end case;
end process DEC_CNT_GEN;
----------------------------------------------------------------
--**********************************************************
-- LOAD_CNT COMBINATIONAL LOGIC:
--**********************************************************
LOAD_CNT_GEN: process (SLTX_currentState)
begin
case SLTX_currentState is

when 	TX_IDLE 	=>  	LOAD_CNT <= '0';
when	START_LO_WAIT	=>  	LOAD_CNT <= '0';
when	START_HI_WAIT	=>	LOAD_CNT <= '0';
when	LOAD_SEND_CT	=>  	LOAD_CNT <= '1';
when	SEND_LO_WAIT	=> 	LOAD_CNT <= '0';
when	SEND_HI_WAIT	=>	LOAD_CNT <= '0';
when	DEC_SEND_CT	=>	LOAD_CNT <= '0';
when	LOAD_DEL_CT	=>	LOAD_CNT <= '1';
when	DEL_LO_WAIT	=>	LOAD_CNT <= '0';
when	DEL_HI_WAIT	=>  	LOAD_CNT <= '0';
when	DEC_DEL_CT	=>  	LOAD_CNT <= '0';
when  CLR_ZEROCT_DV =>  LOAD_CNT <= '0';
when	LOAD_END_CT	=>  	LOAD_CNT <= '1';
when	END_LO_WAIT	=>  	LOAD_CNT <= '0';
when	END_HI_WAIT	=>  	LOAD_CNT <= '0';
when	DEC_END_CT	=>  	LOAD_CNT <= '0';
when	END_XMIT	=>  	LOAD_CNT <= '0';
when 	others 		=>  	LOAD_CNT <= '0';
end case;
end process LOAD_CNT_GEN;
----------------------------------------------------------------

--**********************************************************
 --CLR_DV COMBINATIONAL LOGIC:
--**********************************************************
CLR_DV_GEN: process (SLTX_currentState)
begin
case SLTX_currentState is
when 	TX_IDLE 	=>  	CLR_DV <= '0';
when	START_LO_WAIT	=>  	CLR_DV <= '0';
when	START_HI_WAIT	=>	CLR_DV <= '0';
when	LOAD_SEND_CT	=>  	CLR_DV <= '1';
when	SEND_LO_WAIT	=> 	CLR_DV <= '0';
when	SEND_HI_WAIT	=>	CLR_DV <= '0';
when	DEC_SEND_CT	=>	CLR_DV <= '0';
when	LOAD_DEL_CT	=>	CLR_DV <= '0';
when	DEL_LO_WAIT	=>	CLR_DV <= '0';
when	DEL_HI_WAIT	=>  	CLR_DV <= '0';
when	DEC_DEL_CT	=>  	CLR_DV <= '0';
when  CLR_ZEROCT_DV =>  	CLR_DV <= '1';
when	LOAD_END_CT	=>  	CLR_DV <= '0';
when	END_LO_WAIT	=>  	CLR_DV <= '0';
when	END_HI_WAIT	=>  	CLR_DV <= '0';
when	DEC_END_CT	=>  	CLR_DV <= '0';
when	END_XMIT	=>  	CLR_DV <= '0';
when 	others 		=>  	CLR_DV <= '0';
end case;
end process CLR_DV_GEN;
----------------------------------------------------------------

--**********************************************************
-- SL_TX SYNCHRONOUS OUTPUT:
--**********************************************************
SL_TX_OUTPUT: process(RST_I, CLK_I,PBEN_I, ENA_TX, SL_TX_out) begin
      if (RST_I = '1' or PBEN_I ='0' or ENA_TX = '0') then SL_TX <= '1';
       elsif (CLK_I'event and CLK_I = '1') then 
		SL_TX <= SL_TX_out;
      end if;
end process SL_TX_OUTPUT;

SL_TX_CTRL:process(SLTX_currentState) begin
case SLTX_currentState is
when 	TX_IDLE 	=>  	SL_TX_out <= '1';
when	START_LO_WAIT	=>  	SL_TX_out <= '1';
when	START_HI_WAIT	=>  	SL_TX_out <= '1';
when	LOAD_SEND_CT	=>  	SL_TX_out <= '0';
when	SEND_LO_WAIT	=>  	SL_TX_out <= '0';
when	SEND_HI_WAIT	=>  	SL_TX_out <= '0';
when	DEC_SEND_CT	=>  	SL_TX_out <= '0';
when	LOAD_DEL_CT	=>	SL_TX_out <= '1';
when	DEL_LO_WAIT	=>	SL_TX_out <= '1';
when	DEL_HI_WAIT	=>  	SL_TX_out <= '1';
when	DEC_DEL_CT	=>  	SL_TX_out <= '1';
when  CLR_ZEROCT_DV =>  SL_TX_out <= '1';
when	LOAD_END_CT	=>  	SL_TX_out <= '1';
when	END_LO_WAIT	=>  	SL_TX_out <= '1';
when	END_HI_WAIT	=>  	SL_TX_out <= '1';
when	DEC_END_CT	=>  	SL_TX_out <= '1';
when	END_XMIT	=>  	SL_TX_out <= '1';
when 	others 		=>  	SL_TX_out <= '1';
end case;
end process SL_TX_CTRL;
----------------------------------------------------------------

--**********************************************************
-- SL_RX Delay LOGIC:
--**********************************************************
SL_RX_SYNCH: process (CLK_I, RST_I,PBEN_I, ENA_TX, SL_RX,SL_RX_D1,SSL_RX)
begin
if (RST_I = '1' or PBEN_I = '0' or ENA_TX = '0') then 
			SL_RX_D1 <= '1';
			SSL_RX <= '1';
elsif (CLK_I'event and CLK_I = '1') then 
			SL_RX_D1 <= SL_RX;
			SSL_RX <= SL_RX_D1;
end if;
end process SL_RX_SYNCH;
----------------------------------------------------------------
--**********************************************************
-- SL_TX Delay LOGIC:
--**********************************************************
SL_TX_DELAY: process (CLK_I, RST_I,PBEN_I, ENA_TX, SL_TX_out,SL_TX_D1,SL_TX_D2,SL_TX_D3,SL_TX_D4,SL_TX_D5)
begin
if (RST_I = '1' or PBEN_I = '0' or ENA_TX = '0') then 
			SL_TX_D1 <= '1';
			SL_TX_D2 <= '1';
			SL_TX_D3 <= '1';
			SL_TX_D4 <= '1';
			SL_TX_D5 <= '1';

elsif (CLK_I'event and CLK_I = '1') then 
			SL_TX_D1 <= SL_TX_out;
			SL_TX_D2 <= SL_TX_D1;
			SL_TX_D3 <= SL_TX_D2;
			SL_TX_D4 <= SL_TX_D3;
			SL_TX_D5 <= SL_TX_D4;
end if;
end process SL_TX_DELAY;
----------------------------------------------------------------



--**********************************************************
-- SL_TC SYNCHRONOUS OUTPUT:
--**********************************************************
-- Assert SL_TC when a low is detected on the receive line during delimiter

SL_TC_OUTPUT: process(RST_I, CLK_I,PBEN_I, ENA_TX, SL_TC_out) begin
      if (RST_I = '1' or PBEN_I = '0' or ENA_TX = '0') then SL_TC <= '0';
       elsif (CLK_I'event and CLK_I = '1') then 
		SL_TC <= SL_TC_out;
      end if;
end process SL_TC_OUTPUT;

SL_TC_CTRL:process(SLTX_currentState,SL_TX_D1,SL_TX_D5,SSL_RX) begin
case SLTX_currentState is
when 	TX_IDLE 	=>  	SL_TC_out <= '0';
when	START_LO_WAIT	=>  	SL_TC_out <= '0';
when	START_HI_WAIT	=>  	SL_TC_out <= '0';
when	LOAD_SEND_CT	=>  	SL_TC_out <= '0';
when	SEND_LO_WAIT	=>  	SL_TC_out <= '0';
when	SEND_HI_WAIT	=>  	SL_TC_out <= '0';
when	DEC_SEND_CT	=>  	SL_TC_out <= '0';
when	LOAD_DEL_CT	=>	SL_TC_out <= '0';
when	DEL_LO_WAIT	=>  	SL_TC_out <= '0'; -- removed detect 2-23-04
when	DEL_HI_WAIT	=>  	SL_TC_out <= SL_TX_D1 and SL_TX_D5 and not SSL_RX;  -- collision
when	DEC_DEL_CT	=>  	SL_TC_out <= '0';  -- removed detect 2-23-04
when  CLR_ZEROCT_DV =>  SL_TC_out <= '0';
when	LOAD_END_CT	=>  	SL_TC_out <= '0';
when	END_LO_WAIT	=>  	SL_TC_out <= '0';
when	END_HI_WAIT	=>  	SL_TC_out <= '0';
when	DEC_END_CT	=>  	SL_TC_out <= '0';
when	END_XMIT	=>  	SL_TC_out <= '0';
when 	others 		=>  	SL_TC_out <= '0';
end case;
end process SL_TC_CTRL;
----------------------------------------------------------------


--**********************************************************
-- TX_READY SYNCHRONOUS OUTPUT:
--**********************************************************
-- TX_READY indicates NEXT tx value CAN be WRITTEN TO register

TX_READY_OUTPUT: process(RST_I, CLK_I,PBEN_I, ENA_TX, TX_READY_out) begin
--      if (RST_I = '1' or PBEN_I = '0' or ENA_TX = '0') then TX_READY <= '0';
-- avoid initial interrupt by letting TX_READY be high initially 3-1-04
      if (RST_I = '1' or PBEN_I = '0' or ENA_TX = '0') then TX_READY <= '1';
       elsif (CLK_I'event and CLK_I = '1') then 
		TX_READY <= TX_READY_out;
      end if;
end process TX_READY_OUTPUT;


-- 3-1-04 Modified TX_READY to remain high whenever data may be written to tx
TX_READY_out <= not LXDV;

-- old code prior to 3-1
-- Assert TX_READY when EACH VALUE IS READ INTO TRANSMITTER
--TX_READY_out <= CLR_DV;
----------------------------------------------------------------



	
	





end rtl;

