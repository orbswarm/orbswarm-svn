/* TS7250 EEPROM info */
#define SSPCR1           0x04
#define SSPCPSR          0x10
#define CS_MSK           0x02
#define SSP_DATA         0x08

#define SPI_PAGE    0x808a0000
#define PLD_PAGE    0x23000000

static unsigned long spistart;  

// Write Disable
void ee_wrdi(unsigned long dr_page) {

	POKE32(spistart + 0x8, 0x4);                     // push command byte
	POKE32(spistart + 0x4, 0x10);                    // start transmit
	while ((PEEK32(spistart + 0xc) & 0x10) == 0x10); // wait for completion
	PEEK32(spistart + 0x8);                          // pop byte 1 (ignore)
	POKE32(spistart + 0x4, 0x0);                     // stop transmit
}

// Write Enable
void ee_wren(unsigned long dr_page) {

	POKE32(spistart + 0x8, 0x6);                     // push command byte
	POKE32(spistart + 0x4, 0x10);                    // start transmit
	while ((PEEK32(spistart + 0xc) & 0x10) == 0x10); // wait for completion
	PEEK32(spistart + 0x8);                          // pop byte 1 (ignore)
	POKE32(spistart + 0x4, 0x0);                     // stop transmit
}

// Read Status Register
unsigned long ee_rdsr(unsigned long dr_page) {
	unsigned long sreg;
	
	POKE32(spistart + 0x8, 0x5);                     // push command byte
	POKE32(spistart + 0x8, 0x0);                     // push for recv space
	POKE32(spistart + 0x4, 0x10);                    // start transmit
	while ((PEEK32(spistart + 0xc) & 0x10) == 0x10); // wait for completion
	
	PEEK32(spistart + 0x8);                          // pop byte 1 (ignore)
	sreg = PEEK32(spistart + 0x8) & 0xff;            // pop recv'd status
	POKE32(spistart + 0x4, 0x0);                     // stop transmit
	return sreg;
}

// Write Status Register
void ee_wrsr(unsigned long dr_page, unsigned long sreg) {

	POKE32(spistart + 0x8, 0x1);                     // push command byte
	POKE32(spistart + 0x8, sreg);                    // push new status
	POKE32(spistart + 0x4, 0x10);                    // start transmit
	while ((PEEK32(spistart + 0xc) & 0x10) == 0x10); // wait for completion
	PEEK32(spistart + 0x8);                          // pop byte 1 (ignore)
	PEEK32(spistart + 0x8);                          // pop byte 2 (ignore)
	POKE32(spistart + 0x4, 0x0);                     // stop transmit
}

// Write to Memory Array
void ee_write_byte(unsigned long dr_page, unsigned long addr, 
	unsigned char *dat) {

	POKE32(spistart + 0x8, 0x2);                     // push command byte
	POKE32(spistart + 0x8, (addr & 0xff00) >> 8);    // push addr msb 
	POKE32(spistart + 0x8, (addr & 0xff));           // push addr lsb
	POKE32(spistart + 0x8, dat[0]);                  // push data byte 0
	POKE32(spistart + 0x4, 0x10);                    // start transmit
	while ((PEEK32(spistart + 0xc) & 0x10) == 0x10); // wait for completion
	PEEK32(spistart + 0x8);                          // pop byte 1 (ignore)
	PEEK32(spistart + 0x8);                          // pop byte 2 (ignore)
	PEEK32(spistart + 0x8);                          // pop byte 3 (ignore)
	PEEK32(spistart + 0x8);                          // pop byte 4 (ignore)
	POKE32(spistart + 0x4, 0x0);                     // stop transmit
}

// Read from Memory Array
void ee_read_byte(unsigned long dr_page, unsigned long addr, 
	unsigned char *dat) {

	POKE32(spistart + 0x8, 0x3);                     // push command byte
	POKE32(spistart + 0x8, (addr & 0xff00) >> 8);    // push addr msb 
	POKE32(spistart + 0x8, (addr & 0xff));           // push addr lsb
	POKE32(spistart + 0x8, 0x0);                     // push for recv space
	POKE32(spistart + 0x4, 0x10);                    // start transmit
	while ((PEEK32(spistart + 0xc) & 0x10) == 0x10); // wait for completion

	PEEK32(spistart + 0x8);                          // pop byte 1 (ignore)
	PEEK32(spistart + 0x8);                          // pop byte 2 (ignore)
	PEEK32(spistart + 0x8);                          // pop byte 3 (ignore)
	dat[0] = PEEK32(spistart + 0x8);                 // pop data byte 1
	POKE32(spistart + 0x4, 0x0);                     // stop transmit

}
