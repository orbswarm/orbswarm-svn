package com.orbswarm;

import java.io.IOException;
import java.io.OutputStream;
import java.util.TooManyListenersException;

import javax.comm.CommPortIdentifier;
import javax.comm.NoSuchPortException;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.SerialPortEvent;
import javax.comm.SerialPortEventListener;
import javax.comm.UnsupportedCommOperationException;

public class SerialWriter {
	
	static void write(String strPortIdentifier, byte[] msg) 
	throws NoSuchPortException, PortInUseException, 
	IOException, UnsupportedCommOperationException, TooManyListenersException
	{
		CommPortIdentifier id = CommPortIdentifier.getPortIdentifier(strPortIdentifier);
		if(id == null || id.getPortType() != CommPortIdentifier.PORT_SERIAL)
			throw new RuntimeException("serial port not found");
		SerialPort port = (SerialPort)id.open("swarm aggregator test harness", 5000);
		try{
			OutputStream os = port.getOutputStream();
			try{
				//8 data bits, no parity, one stop bit, and no hardware flow control., 19200 bps
				port.setSerialPortParams(19200, 8, 1, 0);
				port.notifyOnOutputEmpty(true);
				_Listener l = new _Listener();
				port.addEventListener(l);
				os.write(msg);
				l.waitTillXferOver();
			}
			finally{
				os.close();
			}
		}
		finally{
			port.close();
		}
	}
	
	static class _Listener implements SerialPortEventListener
	{
		private volatile boolean m_isXferOver=false;
		public synchronized void serialEvent(SerialPortEvent ev) 
		{
			if(ev.equals(SerialPortEvent.OUTPUT_BUFFER_EMPTY))
					m_isXferOver=true;
			notifyAll();
		}
		
		public synchronized void waitTillXferOver()
		{
			while(!m_isXferOver)
			{
				try{
					wait();
				}
				catch(InterruptedException ex){
					//do nothing
				}
			}
		}
	}
}
