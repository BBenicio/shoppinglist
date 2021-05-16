package io.benic.shoppinglist.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.benic.shoppinglist.model.AppDatabase
import io.benic.shoppinglist.model.ItemDao
import io.benic.shoppinglist.model.Migrations
import io.benic.shoppinglist.model.ShoppingCartDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Singleton
    @Provides
    fun provideDb(app: Application): AppDatabase {
        return Room.databaseBuilder(app, AppDatabase::class.java, "shopping_cart")
            .addMigrations(Migrations.migrationFrom1To2)
            .addMigrations(Migrations.migrationFrom2To3)
            .addMigrations(Migrations.migrationFrom3To4)
            .addMigrations(Migrations.migrationFrom4To5)
            .addMigrations(Migrations.migrationFrom5To6)
            .build()
    }

    @Singleton
    @Provides
    fun provideShoppingCartDao(db: AppDatabase): ShoppingCartDao {
        return db.shoppingCartDao()
    }

    @Singleton
    @Provides
    fun provideItemDao(db: AppDatabase): ItemDao {
        return db.itemDao()
    }
}