#Build shared lib first
LIB_SRC= $(SVN_TRUNK)/aggregator/aggregator_src/c/shared
LIB_BIN = $(SVN_TRUNK)/aggregator/binaries/shared
LIB_BIN_GLOBAL = $(LIB_BIN)/global
build_lib: $(LIB_BIN)/timer0.o $(LIB_BIN)/uart.o $(LIB_BIN)/spu.o $(LIB_BIN_GLOBAL)/swarm_messaging.o $(LIB_BIN_GLOBAL)/xbee.o  $(LIB_BIN_GLOBAL)/gps.o
#build_lib: $(LIB_BIN)/timer0.o
		
$(LIB_BIN)/%.o : $(LIB_SRC)/%.c	
	@echo  '-----Building libraries--------'
#	avr-gcc 	-c -I. -I $(GLOBAL_SHARED_INCLUDE) -g -Os 	-funsigned-char \
#	-funsigned-bitfields -fpack-struct 	\
#	-fshort-enums -Wall -Wstrict-prototypes -DF_CPU=$(F_CPU) \
# 	-Wa,-adhlns=$*.lst  -mmcu=$(MCU) -std=gnu99 $< -o $@
		$(CC) -c $(ALL_CFLAGS)  $< -o $@

$(LIB_BIN_GLOBAL)/%.o : $(GLOBAL_SHARED_SRC)/%.c
#		avr-gcc 	-c -I. -I $(GLOBAL_SHARED_INCLUDE) -g -Os 	-funsigned-char \
#	-funsigned-bitfields -fpack-struct 	\
#	-fshort-enums -Wall -Wstrict-prototypes -DF_CPU=$(F_CPU) \
# 	-Wa,-adhlns=$*.lst  -mmcu=$(MCU) -std=gnu99 $< -o $@
	@echo  '-----Building global libraries--------'
	$(CC) -c $(ALL_CFLAGS)  $< -o $@

clean_lib:
	rm -f $(LIB_BIN)/*.o
	rm -f $(LIB_BIN_GLOBAL)/*.o 

