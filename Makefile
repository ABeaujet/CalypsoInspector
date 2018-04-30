
all: CalypsoInspector CalypsoFileFinder

CFF_PACKAGE=fr/mikado/calypsofilefinder
CIN_PACKAGE=fr/mikado/calypsoinspector
DEP_PACKAGE=fr/mikado/isodepimpl

COM_UNITS=$(addprefix	fr/mikado/calypso/,	BitArray*				\
											CalypsoCard*			\
											CalypsoEnvironment*		\
											CalypsoFile*			\
											CalypsoRawDump*			\
											CalypsoRecordField*		\
											CalypsoRecord*			\
											SWDecoder*			)	\
		  $(addprefix	fr/mikado/isodep/,	CardException*			\
											CommandAPDU*			\
											IsoDepInterface*		\
											ResponseAPDU*		)	\
		  $(addprefix	fr/mikado/xmlio/,	XMLIOImpl*				\
											XMLIOInterface*			)

CFF_UNITS=$(addprefix	$(CFF_PACKAGE)/,	CalypsoCardSearch*		\
											Main*				)	\
		  $(addprefix	$(DEP_PACKAGE)/,	IsoDepImpl*				)

CIN_UNITS=$(addprefix	$(CIN_PACKAGE)/,	CalypsoDump*			\
		  									Main*				)	\
		  $(addprefix	$(DEP_PACKAGE)/,	IsoDepImpl*				)

COM_SOURCES=$(addsuffix .java,	$(COM_UNITS))
CFF_SOURCES=$(addsuffix .java,	$(CFF_UNITS))
CIN_SOURCES=$(addsuffix .java,	$(CIN_UNITS))

COM_BIN=$(addsuffix .class,	$(COM_UNITS))
CFF_BIN=$(addsuffix .class,	$(CFF_UNITS))
CIN_BIN=$(addsuffix .class,	$(CIN_UNITS))

JDOMLIB=lib/jdom2-2.0.3.jar
JDOMURL=http://www.java2s.com/Code/JarDownload/jdom2/jdom2-2.0.3.jar.zip

$(JDOMLIB):
	mkdir -p lib
	wget $(JDOMURL) -O $(JDOMLIB).zip
	unzip $(JDOMLIB).zip -d ./lib
	rm $(JDOMLIB).zip

out/libcalypso.jar: $(JDOMLIB) $(addprefix calypso/src/, $(COM_SOURCES))
	mkdir -p out
	cd calypso/src && javac -cp ../../$(JDOMLIB) $(COM_SOURCES)
	cd calypso/src && jar cvf ../../out/libcalypso.jar $(COM_BIN)

CalypsoFileFinder: out/libcalypso.jar $(addprefix src/, $(CFF_SOURCES))
	cd src && javac -cp ../$(JDOMLIB):../out/libcalypso.jar $(CFF_SOURCES)
	cd src && jar cvf ../out/CalypsoFileFinder.jar $(CFF_BIN)

CalypsoInspector: out/libcalypso.jar $(addprefix src/, $(CIN_SOURCES))
	cd src && javac -cp ../out/libcalypso.jar $(CIN_SOURCES)
	cd src && jar cvf ../out/CalypsoInspector.jar $(CIN_BIN)
	
clean:
	@find . -type f | grep \.class$$ | while read l; do echo Deleting $$l; rm -f $$l; done;

reset: clean
	@find out lib -type f | grep \.jar$$ | while read l; do echo Deleting $$l; rm -f $$l; done;
	@echo Deleting out/; rm -rf out/
	@echo Deleting lib/; rm -rf lib/
