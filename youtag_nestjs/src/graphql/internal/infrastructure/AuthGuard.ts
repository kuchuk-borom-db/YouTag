import {
  CanActivate,
  ExecutionContext,
  Injectable,
  Logger,
} from '@nestjs/common';
import { GqlContextType } from '@nestjs/graphql';
import { AuthCommander } from '../../../commander/api/Services';

@Injectable()
export class AuthGuard implements CanActivate {
  constructor(private readonly authCommander: AuthCommander) {}

  private log = new Logger(AuthGuard.name);

  async canActivate(context: ExecutionContext): Promise<boolean> {
    if (context.getType<GqlContextType>() !== 'graphql') {
      return false;
    }
    const args = context.getArgs();
    const request = args[2].req;
    const headers = request.headers;
    console.log(headers);
    if (headers['authorization'] === null) {
      this.log.warn('Missing authorization header');
      return false;
    }
    const tokenRaw: string = headers['authorization'];
    if (!tokenRaw.startsWith('Bearer ')) {
      this.log.error('Authorization header is not bearer token');
      return false;
    }
    const token = tokenRaw.substring(7);
    const user = await this.authCommander.validateAccessToken(token);
    if (!user) {
      this.log.warn('User is null from token subject');
      return false;
    }
    request.user = user;
    return true;
  }
}
