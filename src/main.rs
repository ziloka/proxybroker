use std::ffi::OsString;
use clap::{Arg, Command, SubCommand};

mod commands;
mod services;

// https://stackoverflow.com/questions/54837057/how-can-i-display-help-after-calling-claps-get-matches
// https://docs.rs/clap/latest/src/git/git.rs.html#34
#[tokio::main]
async fn main() {

  let mut app = Command::new("ProxyBroker")
    .about("Serves foos to the world!")
    .version("v0.1.0")
    .author("Ziloka (@Ziloka on GitHub)")
    .subcommand(SubCommand::with_name("find")
      .about("Find and check proxies")
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
      commands::find::find(sub_matches).await;
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