import { Module } from '@nestjs/common';
import { GraphQLModule } from '@nestjs/graphql';
import { ApolloDriver, ApolloDriverConfig } from '@nestjs/apollo';
import { ApolloServerPluginLandingPageLocalDefault } from '@apollo/server/plugin/landingPage/default';
import { ConfigModule, ConfigService } from '@nestjs/config';
// eslint-disable-next-line @typescript-eslint/no-restricted-imports
import { UserModule } from './user/user.module';
import { TypeOrmModule } from '@nestjs/typeorm';
import { EnvironmentConst } from './Utils/Constants';

@Module({
  imports: [
    ConfigModule.forRoot({
      envFilePath: '.env',
      isGlobal: true,
    }),
    TypeOrmModule.forRootAsync({
      imports: [ConfigModule],
      inject: [ConfigService],

      useFactory: (env: ConfigService) => {
        return {
          type: 'postgres',
          host: env.get<string>(EnvironmentConst.Db.Host),
          username: env.get<string>(EnvironmentConst.Db.Username),
          password: env.get<string>(EnvironmentConst.Db.Password),
          database: env.get<string>(EnvironmentConst.Db.DatabaseName),
          ssl: {
            rejectUnauthorized: true,
          },
          autoLoadEntities: true,
        };
      },
    }),
    GraphQLModule.forRoot<ApolloDriverConfig>({
      driver: ApolloDriver,
      autoSchemaFile: true,
      playground: false, // Disable default playground
      plugins: [ApolloServerPluginLandingPageLocalDefault()], // Use Apollo Sandbox
    }),
    UserModule,
  ],
})
export class AppModule {}
