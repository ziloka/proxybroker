use std::ffi::OsString;
use clap::{Arg, Command, SubCommand};

pub mod commands;
pub mod services;

// https://docs.rs/clap/latest/src/git/git.rs.html#34
// https://stackoverflow.com/questions/54837057/how-can-i-display-help-after-calling-claps-get-matches
// https://github.com/clap-rs/clap/blob/master/examples/tutorial_builder/04_02_parse.rs
#[tokio::main]
async fn main() {

  let mut app = Command::new("ProxyBroker")
    .about("Serves foos to the world!")
    .version("v0.1.0")
    .author("Ziloka (@Ziloka on GitHub)")
    .subcommand(SubCommand::with_name("find")
      .about("Find and check proxies")
      .arg(Arg::with_name("limit")
        .long("limit")
        .short('l')
        .takes_value(true)
        .default_value("10")
        .default_missing_value("10")
        .value_parser(clap::value_parser!(u64).range(1..))
        .help("Number of proxies to check and collect"))
      .arg(Arg::with_name("file")
        .long("file")
        .short('f')
        .takes_value(true)
        .help("Send proxies to file")));

  let mut help = Vec::new();
  app.write_long_help(&mut help).unwrap();
  let matches = app.get_matches();

  match matches.subcommand() {
    Some(("find", sub_matches)) => {
      proxybroker::commands::find::find(sub_matches);
    },
    Some((ext, sub_matches)) => {
      let args = sub_matches
          .get_many::<OsString>("")
          .into_iter()
          .flatten()
          .collect::<Vec<_>>();
      println!("Calling out to {:?} with {:?}", ext, args);
    }
    None => {
      println!("{}", std::str::from_utf8(&help).unwrap());
    }
  }
}