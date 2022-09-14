import { EventEmitter } from "events";
import { Client } from 'undici';
import { Proxy } from '../services/collector'

export async function check(emitter: EventEmitter, proxy: Proxy) {
  try {
    let client = new Client(`http://${proxy.host}:${proxy.port}`);
    let response = await client.request({
      path: 'http://httpbin.org/ip?json',
      method: 'GET'
    });
    response.body.setEncoding('utf8');
    let data = '';
    for await (const chunk of response.body) data += chunk;
    let obj = JSON.parse(data);
    emitter.emit('foundCheckedProxies', {
      alive: obj.origin === proxy.host,
      host: proxy.host,
      port: proxy.port
    })
  } catch(e) {

  }
}