import undici from "undici"
import { EventEmitter } from "events";

export interface Proxy {
  host: string,
  port: number
}

async function getProxiesFromSite(emitter: EventEmitter, proxySource: { url: string, protocol: string }) {
  try {
    let body = await (await undici.fetch(proxySource.url)).text();
     emitter.emit('foundUncheckedProxies', body.split("\n").map((s) => {
      let splits = s.split(":");
      return { host: splits[0], port: +splits[1] }
     }));
  } catch(e) {
    // console.log(e);
  }
}

export function collect(emitter: EventEmitter) {
  for (let proxySource of require('../../assets/sources.json')) {
    getProxiesFromSite(emitter, proxySource);
  }
}