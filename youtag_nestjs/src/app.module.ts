import {Module} from '@nestjs/common';
import {GraphQLModule} from '@nestjs/graphql';
import {ApolloDriver, ApolloDriverConfig} from '@nestjs/apollo';
import {ApolloServerPluginLandingPageLocalDefault} from '@apollo/server/plugin/landingPage/default';
import {ConfigModule, ConfigService} from '@nestjs/config';
import {UserModule} from './user/user.module';
import {TypeOrmModule} from '@nestjs/typeorm';
import {EnvironmentConst} from './Utils/Constants';
import {VideoModule} from './video/video.module';
import {TagModule} from './tag/tag.module';
import {GraphqlModule} from './graphql/graphql.module';
import {CommanderModule} from './commander/commander.module';
import * as path from 'node:path';
import * as process from 'node:process';
import {CacheModule} from '@nestjs/cache-manager';

@Module({
    imports: [
        CacheModule.register({
            isGlobal: true,
            ttl: 240000, //240 sec
        }),
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
                    //synchronize: true, //only for development
                };
            },
        }),
        GraphQLModule.forRoot<ApolloDriverConfig>({
            driver: ApolloDriver,
            typePaths: ['./**/*.graphql'],
            definitions: {
                path: path.join(process.cwd(), 'src/graphql.ts'),
                outputAs: 'class',
            },
            playground: false, // Disable default playground
            plugins: [ApolloServerPluginLandingPageLocalDefault()], // Use Apollo Sandbox
            context: () => ({})
        }),
        UserModule,
        VideoModule,
        TagModule,
        GraphqlModule,
        CommanderModule,
    ],
})
export class AppModule {
}
