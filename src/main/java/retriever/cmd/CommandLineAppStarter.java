package retriever.cmd;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import retriever.db.DBHelper;

@Component
public class CommandLineAppStarter implements CommandLineRunner {

    @Autowired
    private DBHelper dbHelper;

    private static final int LOG_FREQUENCY = 1000;

    @Override
    public void run(String... args) throws Exception {
        Options options = new Options();
        options.addOption("p", "parse", false, "only parse blocks");
        options.addOption("s", true, "start block number");
        options.addOption("f", true, "last block number");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        int startBlockIndex;

        if (cmd.hasOption("s")) {
            startBlockIndex = Integer.parseInt(cmd.getOptionValue("s"));
        } else {
            startBlockIndex = -1;
        }

        int lastBlockIndex;
        if (cmd.hasOption("f")) {
            lastBlockIndex = Integer.parseInt(cmd.getOptionValue("f"));
        } else {
            lastBlockIndex = dbHelper.getLastBlockIndex();
        }

        if (startBlockIndex == -1)
            return;

        if (!cmd.hasOption("p")) {
            System.out.println("Started copying from " + startBlockIndex + " to " + lastBlockIndex);
            for (int i = startBlockIndex; i <= lastBlockIndex; i++) {
                dbHelper.copyBlockToDB(i);
                if (i % LOG_FREQUENCY == 0)
                    System.out.println("Copied " + i + " blocks");
            }
        } else {
            System.out.println("Started parsing from " + startBlockIndex + " to " + lastBlockIndex);
            for (int i = startBlockIndex; i <= lastBlockIndex; i++) {
                dbHelper.parseBlockToDB(i);
                if (i % LOG_FREQUENCY == 0)
                    System.out.println("Parsed " + i + " blocks");
            }
        }

    }

}
