import {NestFactory} from '@nestjs/core';
import {AppModule} from './app.module';
import {FastifyAdapter, NestFastifyApplication,} from '@nestjs/platform-fastify';

async function bootstrap() {
    const app = await NestFactory.create<NestFastifyApplication>(
        AppModule,
        new FastifyAdapter(),
    );

    const corsOrigins = process.env.CORS_ORIGINS?.split(',')
    app.enableCors({
        origin: corsOrigins,
        credentials: true,
    });
    //Open server to all devices connected to the same network
    await app.listen(process.env.PORT ?? 3000, "0.0.0.0");
}

bootstrap();
