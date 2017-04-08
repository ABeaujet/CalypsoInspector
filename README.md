# CalypsoInspector

This tools reads Calypso smartcards and tries to parse data. It is primarily aimed at decoding data inside public transport cards complying to the Intercode standard.

This is essentially based on ISO7816-4 and some pieces of information here and there (access to official documentation for Intercode/Intertic is charged 136.35€).

The [Android port](http://github.com/ABeaujet/CalypsoInspectorAndroid) is under development. It may eventually support external OTG readers (ACR122U) in the near future.

This project is tested with the cards used in Lille, France (PassPass or Pass Pass cards). It should also work with Navigo cards, though some changes are necessary due to the complex topology of Paris public transport network (zones, check-outs...).
I will make this project compatible with Navigo cards once I get a new one, but that shouldn't be that hard.

NONE OF THE INFORMATION IN THIS REPO SHOULD BE TAKEN AS FACE VALUE. Just in case.

## Currently available data :

- Environment
- Holder information
- Holder profiles
- Latest check-ins (date, time, route, stop, direction, contract)
- Contract list
- File Mappings : some files (like contracts) may have several different layouts depending on other fields.

## TODO :
- Support for more CalypsoRecordField.Type
- Android port (it's being done [here](http://github.com/ABeaujet/CalypsoInspectorAndroid))
- Android support for external readers

## How to use:

First, if you don't know the card structure, you may want to build one based on educated guesses using CalypsoFileFinder (see below).

Each public transport operator has its own ticketing policy and thus card structure. But in practice, LFIs don't change that much, and the content should be parsed quite nicely out of the box (except for route and stop names which are network dependant).

For optimal results, copy and customize `networks/250/149/cardstruct.xml` with your own card structure (maybe you'll find some PDFs here and there from your network operator).
Then, copy and customize `networks/250/149/topology.xml` to add your own metro/bus/... stops and routes. Again, interesting PDFs can be found in non .htaccess protected directories. Time to put your Google-fu to the test ;)


# CalypsoFileFinder:

This utility enumerates all the files contained on the card (option #1), and allows you to search for bit patterns in the files (#2).
If a card structure is loaded, you can search through record fields as well, and get an nice and readable output.

Thus, rebuilding the card structure becomes quite easy.

First, you have to list enumerate the files on the card using CalypsoFileFinder (option #1).
Then, edit `fileList.out.xml` to configure the card structure (DFs, file descriptions, remove useless files..) and rename it to `fileList.xml` in order to search through those files.

Once you're done, edit `fileList.xml` to make it look like the default `cardstruct.xml`, move it to `networks/$countryCode/$networkId` and add a new entry to `networks/networks.xml`.
Then move on to CalypsoInspector.

# License

Do What the Fuck You Want to Public License (WTFPL)
Though I'd be interested to hear from other people tinkering with Calypso Cards ;)

