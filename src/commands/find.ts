import { Command } from 'commander';
import EventEmitter from 'events';
import { collect, Proxy } from '../services/collector';
import { check } from '../services/checker';

export function find(str: { limit: string }, options: Command) {

  const limit = +str.limit;
  const emitter = new EventEmitter();
  let counter = 0;

  collect(emitter);

  emitter.on('foundUncheckedProxies', (proxies: Proxy[]) => {
    proxies.map((p) => check(emitter, p));
  }).on('foundCheckedProxies', (proxy) => {
    if (proxy.alive) {
      counter++;
      console.log(`${proxy.host}:${proxy.port}`)
      if (counter == limit) {
        process.exit();
      }
    }
  });

}