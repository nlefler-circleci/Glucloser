package com.nlefler.glucloser.util.database.upgrade;

public class OneToTwo extends DatabaseUpgrader {
	
//	protected String[] getUpgradeCommands() {
//		return new String[] {
//				// Add PlaceToFoodsHash column to SyncInfoDown
//				"CASE WHEN SELECT " + Tables.PLACE_TO_FOODS_HASH_DB_NAME +
//				" FROM " + Tables.SYNC_INFO_DOWN_DB_NAME + " THEN NULL ELSE " +
//				" ALTER TABLE " + Tables.SYNC_INFO_DOWN_DB_NAME +
//				" ADD COLUMN " + Tables.PLACE_TO_FOODS_HASH_DB_NAME +
//				" TEXT END;",
//				// Add PlaceToFoodsHash column to SyncInfoUp
//				"CASE  WHEN SELECT " + Tables.PLACE_TO_FOODS_HASH_DB_NAME +
//				" FROM " + Tables.SYNC_INFO_UP_DB_NAME + " THEN NULL ELSE" +
//				" ALTER TABLE " + Tables.SYNC_INFO_UP_DB_NAME +
//				" ADD COLUMN " + Tables.PLACE_TO_FOODS_HASH_DB_NAME +
//				" TEXT END;",
//				// Add MealToFoodsHash column to SyncInfoDown
//				"CASE  WHEN SELECT " + Tables.MEAL_TO_FOODS_HASH_DB_NAME +
//				" FROM " + Tables.SYNC_INFO_DOWN_DB_NAME + " THEN NULL ELSE" +
//				" ALTER TABLE " + Tables.SYNC_INFO_DOWN_DB_NAME +
//				" ADD COLUMN " + Tables.MEAL_TO_FOODS_HASH_DB_NAME +
//				" TEXT END;",
//				// Add MealToFoodsHash column to SyncInfoUp
//				"CASE  WHEN SELECT " + Tables.MEAL_TO_FOODS_HASH_DB_NAME +
//				" FROM " + Tables.SYNC_INFO_UP_DB_NAME + " THEN NULL ELSE " +
//				" ALTER TABLE " + Tables.SYNC_INFO_UP_DB_NAME +
//				" ADD COLUMN " + Tables.MEAL_TO_FOODS_HASH_DB_NAME +
//				" TEXT END;"
//		};
//	}
}
