import { Command } from 'commander';
import { find } from './commands/find';

const program = new Command();

program
  .name('proxybroker')
  .description('CLI to get some proxies')
  .version('0.0.1');

program.command('find')
  .description('Get a list of working proxies')
  // .argument('<string>', 'string to split')
  .option('--limit', 'number of proxies to collect', '10')
  // .option('-s, --separator <char>', 'separator character', ',')
  .action((str: { limit: string }, options: Command) => {
    find(str, options);
    // const limit = options.first ? 1 : undefined;
    // console.log(str.split(options.separator, limit));
  });

program.parse();