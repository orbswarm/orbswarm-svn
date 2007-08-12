/*
 * ts_sbc.h
 * Liberty Young	Technologic Systems
 * spam_filter(email): liberty(at)embeddedx86.com/dot/com
*/

#define TS5300	0x50
#define TS5400  0x40
#define TS5500  0x60
#define TS5600	0x20
#define TS5700	0x70
#define TS3400  4
#define TS3300  3
#define TS3200  2
#define TS3100  1

#define TS7200	0x00
#define TS7250  0x01
#define TS7260  0x02
#define TS7300  0x03

#define IO_SBCID 0x74

/*
#ifndef TSYSTEMS
# error	"compilation without -DTSYSTEMS (or the like)"
#endif
*/

#define AUTO485FD 1
#define RTSMODE 2
#define AUTO485HD 4
/*describes the SBC and Options installed*/
struct sbc_info {
	int  board_id;
	char RS485;
	char AtoD;
	char RS422;
	char Ethernet;
	char auto485;
	char extrst; 	//external reset
	char sram; 	
	char industrial;
	char jumpers;
	char pld_ver;
};
	
extern struct sbc_info *get_sbcinfo (void ) ;
extern void remove_sbcinfo (struct sbc_info *info);
