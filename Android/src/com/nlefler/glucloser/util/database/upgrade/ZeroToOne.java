package com.nlefler.glucloser.util.database.upgrade;

import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.sprinkles.annotations.Table;

public class ZeroToOne extends DatabaseUpgrader {
    @Override
    protected String[] getUpgradeCommands() {
        List<String> commands = new ArrayList<String>();
		for (String sql : Tables.tableCreationSQLs) {
            commands.add(sql);
		}

		for (String sql : Indexes.indexCreationSQLs) {
            commands.add(sql);
		}

        for (String sql : Tables.triggerCreationSQLs) {
            commands.add(sql);
        }

        return commands.toArray(new String[0]);
    }
}
