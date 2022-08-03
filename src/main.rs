use std::ffi::OsString;
use clap::{Arg, Command, SubCommand};

mod commands;

// https://docs.rs/clap/latest/src/git/git.rs.html#34
#[tokio::main]
async fn main() {

  let app = Command::new("ProxyBroker")
    .about("Serves foos to the world!")
    .version("v0.1.0")
    .author("Ziloka (@Ziloka on GitHub)")
    .subcommand(SubCommand::with_name("find")
      .about("Runs the Foo Server")
      .arg(Arg::with_name("debug")
        .short('D')
        .help("Sends debug foos instead of normal foos.")));

  let matches = app.get_matches();

  match matches.subcommand() {
    Some(("find", sub_matches)) => {
        let proxies = proxybroker::services::collector::collect().await;
        println!("{}", proxies.len());
        // println!(
        //     "Cloning {}",
        //     sub_matches.get_one::<String>("REMOTE").expect("required")
        // );
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

    }
  }
}