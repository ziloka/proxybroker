use std::path::PathBuf;

use clap::{Parser, Subcommand};

mod collector;

#[derive(Parser)]
#[clap(author, version, about, long_about = None)]
struct Cli {
    /// Optional name to operate on
    #[clap(value_parser)]
    name: Option<String>,

    /// Sets a custom config file
    #[clap(short, long, value_parser, value_name = "FILE")]
    config: Option<PathBuf>,

    /// Turn debugging information on
    #[clap(short, long, action = clap::ArgAction::Count)]
    debug: u8,

    #[clap(subcommand)]
    command: Option<Commands>,
}

#[derive(Subcommand)]
enum Commands {
    Version {},
    Find {},
    Check {},
    Grab {},
    Serve {},
}

#[tokio::main]
async fn main() {
    let cli = Cli::parse();

    // You can check the value provided by positional arguments, or option arguments
    if let Some(name) = cli.name.as_deref() {
        println!("Value for name: {}", name);
    }

    if let Some(config_path) = cli.config.as_deref() {
        println!("Value for config: {}", config_path.display());
    }

    // You can see how many times a particular flag or argument occurred
    // Note, only flags can have multiple occurrences
    match cli.debug {
        0 => println!("Debug mode is off"),
        1 => println!("Debug mode is kind of on"),
        2 => println!("Debug mode is on"),
        _ => println!("Don't be crazy"),
    }

    // You can check for the existence of subcommands, and if found use their
    // matches just as you would the top level cmd
    match &cli.command {
        Some(Commands::Version {}) => {
          println!("test");
        }
        Some(Commands::Find {}) => {
          // async functions only run when they are await'ed
          // https://stackoverflow.com/questions/72724529/print-inside-async-function
          collector::collect().await;
        }
        Some(Commands::Check {}) => {

        }
        Some(Commands::Grab {}) => {

        }
        Some(Commands::Serve {}) => {

        }
        None => {}
    }

}

// use std::collections::HashMap;

// #[tokio::main]
// async fn main() -> Result<(), Box<dyn std::error::Error>> {
//     let resp = reqwest::get("https://httpbin.org/ip")
//         .await?
//         .json::<HashMap<String, String>>()
//         .await?;
//     println!("{:#?}", resp);
//     Ok(())
// }