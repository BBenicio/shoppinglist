package io.benic.shoppinglist.model

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {
    val migrationFrom1To2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("alter table carts add column maxCost INTEGER default 0")
        }
    }

    val migrationFrom2To3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("create index index_items_cartId on items(cartId)")
        }
    }

    val migrationFrom3To4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("alter table items add column position integer default -1 not null")
        }
    }

    val migrationFrom4To5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("alter table carts add column cost INTEGER not null default 0")
            database.execSQL("alter table carts add column description TEXT not null default ''")
        }
    }

    val migrationFrom5To6 = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("alter table carts add column createdAt integer not null default 0")
        }

    }
}