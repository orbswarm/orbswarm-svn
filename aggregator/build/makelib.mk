#Build shared lib first
LIB_SRC= $(SVN_TRUNK)/aggregator/aggregator_src/c/shared
LIB_BIN = $(SVN_TRUNK)/aggregator/binaries/shared
build_lib: $(LIB_BIN)/timer0.o $(LIB_BIN)/uart.o $(LIB_BIN)/xbee.o
#build_lib: $(LIB_BIN)/timer0.o
		
$(LIB_BIN)/%.o : $(LIB_SRC)/%.c	
	@echo  '-----Building libraries--------'
	avr-gcc 	-c -I. -I $(GLOBAL_SHARED_INCLUDE) -g -Os 	-funsigned-char \
	-funsigned-bitfields -fpack-struct 	\
	-fshort-enums -Wall -Wstrict-prototypes -DF_CPU=$(F_CPU) \
 	-Wa,-adhlns=$*.lst  -mmcu=$(MCU) -std=gnu99 $< -o $@
	
clean_lib:
	rm $(LIB_BIN)/*.o	

