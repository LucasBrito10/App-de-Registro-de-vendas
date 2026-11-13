package com.app.project;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.app.project.client.Client;
import com.app.project.client.ClientDao;
import com.app.project.products.Product;
import com.app.project.products.ProductDao;
import com.app.project.sale.Installment;
import com.app.project.sale.Sale;
import com.app.project.sale.SaleDao;
import com.app.project.sale.SaleItem;
import com.app.project.shared.Converters;

@Database(
        entities = {Client.class, Product.class, Sale.class, SaleItem.class, Installment.class},
        version = 2,
        exportSchema = false
)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "app_database";
    private static volatile AppDatabase instance;

    public abstract ClientDao clientDao();

    public abstract ProductDao productDao();

    public abstract SaleDao saleDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME
                    )
                    // Enquanto o app não tem dados reais: ao mudar a versão do esquema, o banco é
                    // apagado e recriado. ANTES de usar em produção, troque por migrações
                    // (addMigrations), senão os dados dos usuários serão perdidos a cada mudança.
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return instance;
    }
}
